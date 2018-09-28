package device;

import java.io.File;
import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.ext.GraphImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.math.StatsAccumulator;

import graph.Topo;
import graph.Type;
import graph.Vertex;
import graph.Vertices;

public class Network implements Runnable {
	private File file;
	private final int DEVICES_SIZE = 1200;
	private final int LOCATIONS_SIZE = 1000;
	private final int MEC_NUM = 16;
	private final int MEC_CAPACITY = 100;
	private final double ALPHA = 3.0;
	private final double GAMMA = 3.0;
	private final double PROCESSING_ENERGY = 0.1;
	
	public Network(String pathname){
		this.file = new File(pathname);
	}
	
	@Override
	public void run() {

		StatsAccumulator accumulator = new StatsAccumulator();
		
		// running n times
		for (int n = 0; n < 1; n++) {
			// initialization
			Devices devices = new Devices();
			Locations locations = new Locations();
			Vertices vertices = new Vertices();
			
			//-----------------------------------Generating Topology-----------------------------------
			Graph<Vertex, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
			GraphImporter<Vertex, DefaultEdge> importer = Topo.createImporter();
			try {
				importer.importGraph(graph, file);
			} catch (ImportException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			System.out.println(graph);
			
			// add switches in the graph to list
			for (Vertex sw : graph.vertexSet()) {
				vertices.idToVertex.put(sw.getId(), sw);
				vertices.switches.add(sw);
			}
			
			// add MEC servers and connect them to switches
			int nodes_num = vertices.idToVertex.keySet().size();
			for (int id = nodes_num; id < nodes_num + MEC_NUM; id++) {
				Vertex mec = new Vertex(id, Type.MEC, MEC_CAPACITY);
				graph.addVertex(mec);
				vertices.idToVertex.put(mec.getId(), mec);
				vertices.mec.add(mec);
				
				// connect the MEC server to a random switch
				Vertex sw = vertices.switches.get(new Random().nextInt(vertices.switches.size()));
				graph.addEdge(mec, sw);
			}
			// System.out.println(graph);
			
			// generate FloydWarshallShortestPaths
			FloydWarshallShortestPaths<Vertex, DefaultEdge> FloydWarshall  = new FloydWarshallShortestPaths<>(graph);
			System.out.println(FloydWarshall.getPath(vertices.mec.get(0), vertices.mec.get(2)).getWeight());
			
			//-----------------------------------Generating-----------------------------------
			
			// the given MEC configuration
			Map<Device, Vertex> ConfigurationMEC = new HashMap<>();
			
			// generating devices
			for (int i = 0; i < DEVICES_SIZE; i++) {
				Device d = new Device();
				devices.put(d.getId(), d);
				do {
					Vertex mec = vertices.mec.get(new Random().nextInt(vertices.mec.size()));
					if(mec.hasCapacity()) {
						d.setAssociatedMEC(mec);
						mec.setServing(mec.getServing() + 1);
						ConfigurationMEC.put(d, mec);
					}
				} while (d.getAssociatedMEC() == null);
			}
			
			// TODO set device connection energy according to distance
			for(Device device : devices.values()) {
				for(Vertex mec : vertices.mec) {
					device.getConnectionEnergy().put(mec, Math.random() * 0.2 + 0.2);
				}
			}
			
			// generating locations
			for (int i = 0; i < LOCATIONS_SIZE;) {
				Location l = new Location();
				for (Device d : devices.values()) {
					if (calculateDistance(d.getX(), d.getY(), l.getX(), l.getY()) <= Device.MAX_DIS_BETWEEN) {
						l.addCoveredby(d);
						// TODO convert distance to accuracy
						double accuracy = Math.random() * 5;
						d.getAccuracies().put(l, accuracy);
						// larger than ground-truth value
						if (Math.random() <= 0.5) {
							d.getIntervals().put(l,
									Range.closed(accuracy - d.getPrecision(), accuracy + d.getPrecision()));
						} 
						// less than ground-truth value
						else {
							d.getIntervals().put(l,
									Range.closed(-accuracy - d.getPrecision(), -accuracy + d.getPrecision()));
						}
					}
				} // end for
				
				// construct groups for location l
				Locations.constructGroups(l, ALPHA, GAMMA);
				System.out.println(l.getGroupsSize());
				System.out.println("getCoveredBy; " + l.getCoveredBy().size());
				if (l.getGroupsSize() > 0) {
					//System.out.println("coveredby: " + l.getCoveredBy().size());
					//System.out.println(l.getGroups());
					//System.out.println(l.getGroupsSize());
					for (Device d : l.getCoveredBy()) {
						d.addCoverage(l);
					}
					locations.put(l.getId(), l);
					i++; // index++
				} else {
					// discard l if l does not have any group constructed
					// System.out.println("fail");
					continue;
				}
			} // end for
			// generation of locations is completed
			System.out.println("size: " + locations.size());
			for(Device device : devices.values()) {
				System.out.println(device + " -> " + device.getAssociatedMEC());
			}
			
			//-----------------------------------Our-----------------------------------	
			System.out.println("select");
			Set<Device> selectedDevices = Selection.devicesSelection(devices, locations);
			System.out.println("selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			System.out.println("adjust");
			Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall);
			accumulator.add(computeTotalCost(selectedDevices, locations));
			
			//-----------------------------------Greedy-MSC-----------------------------------
			
			
			
		} // end for
		
		// print result
		System.out.println("Our = " + accumulator.mean());
	
	} // end method run
	
	public double computeTotalCost(Set<Device> selectedDevices, Locations locations) {
		double consumed = 0;
		for (Device device : selectedDevices) {
			consumed += device.getConnectionEnergy().get(device.getAssociatedMEC());
		}
		for (Location location : locations.values()) {
			consumed += location.getCommunicationEnergy();
			consumed += PROCESSING_ENERGY;
		}
		return consumed;
	}
	
	public double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0));
	}

}
