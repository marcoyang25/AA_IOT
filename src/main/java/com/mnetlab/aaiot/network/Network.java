package com.mnetlab.aaiot.network;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.math.StatsAccumulator;
import com.mnetlab.aaiot.device.Adjustment;
import com.mnetlab.aaiot.device.Device;
import com.mnetlab.aaiot.device.Devices;
import com.mnetlab.aaiot.device.Location;
import com.mnetlab.aaiot.device.Locations;
import com.mnetlab.aaiot.device.Selection;
import com.mnetlab.aaiot.graph.Topo;
import com.mnetlab.aaiot.graph.Type;
import com.mnetlab.aaiot.graph.Vertex;
import com.mnetlab.aaiot.graph.Vertices;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.ext.GraphImporter;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Network implements Runnable {
	private String name;
	private File file;
	private int DEVICES_SIZE = 1200;
	private int LOCATIONS_SIZE = 1000;
	private int MEC_NUM = 20;
	private int MEC_CAPACITY = 80;
	private double ALPHA;
	private double GAMMA;
	private final double PROCESSING_ENERGY = 0.1;
	
	public Network(String name, String pathname, int DEVICES_SIZE, int LOCATIONS_SIZE, int MEC_NUM, double ALPHA,
			double GAMMA) {
		this.name = name;
		this.file = new File(pathname);
		this.DEVICES_SIZE = DEVICES_SIZE;
		this.LOCATIONS_SIZE = LOCATIONS_SIZE;
		this.MEC_NUM = MEC_NUM;
		this.ALPHA = ALPHA;
		this.GAMMA = GAMMA;	
	}
	
	@Override
	public void run() {

		StatsAccumulator accumulator = new StatsAccumulator();
		StatsAccumulator connectionAccumulator = new StatsAccumulator();
		StatsAccumulator communicationAccumulator = new StatsAccumulator();
		StatsAccumulator GMSCaccumulator = new StatsAccumulator();
		StatsAccumulator ESRaccumulator = new StatsAccumulator();
		
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
			// System.out.println(graph);
			
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
					//device.getConnectionEnergy().put(mec, Math.random() * 0.8 + 0.2);
					device.getConnectionEnergy().put(mec, 0.2);
				}
			}
			
			// generating locations
			for (int i = 0; i < LOCATIONS_SIZE;) {
				Location l = new Location();
				for (Device d : devices.values()) {
					double distance = calculateDistance(d.getX(), d.getY(), l.getX(), l.getY());
					if (distance > 2 && distance <= Device.MAX_DIS_BETWEEN) {
						l.addCoveredby(d);
						// TODO convert distance to accuracy
						final double emittedPower = 4 * Math.random() + 28.0;
						double measuredPower = computeMeasuredPower(emittedPower, distance);
						double accuracy = Math.abs(emittedPower - measuredPower);
						d.getAccuracies().put(l, accuracy);
						d.getIntervals().put(l, Range.closed(measuredPower - d.getPrecision() / 2,
								measuredPower + d.getPrecision() / 2));
					}
				} // end for
				
				// construct groups for location l
				Locations.constructGroups(l, ALPHA, GAMMA);
				// System.out.println(l.getGroupsSize());
				// System.out.println("getCoveredBy; " + l.getCoveredBy().size());
				
				if (l.getGroupsSize() > 0) {
					//System.out.println("coveredby: " + l.getCoveredBy().size());
					//System.out.println(l.getGroups());
					//System.out.println("GroupsSize: " + l.getGroupsSize());
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
			
			// System.out.println("size: " + locations.size());
			// for(Device device : devices.values()) {
			// 	 System.out.println(device + " -> " + device.getAssociatedMEC());
			// }
			/*for(Device device : devices.values()) {
				System.out.println(device);
			}*/
			/*for(Location location : locations.values()) {
				System.out.println(location + " " + location.getGroups());
				for(Group group : location.getGroups()) {
					System.out.println(group.getAverageAccuracy(location));
				}
			}*/
			
			//-----------------------------------Our-----------------------------------	
			//System.out.println("devicesSelection selects");
			Set<Device> selectedDevices = Selection.devicesSelection(devices, locations);
			System.out.println("devicesSelection selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall);
			accumulator.add(computeTotalCost(selectedDevices, locations));
			
			/*for (Location location : locations.values()) {
				System.out.println(location.getSelectedGroup().getMembers().size());
			}*/
			connectionAccumulator.add(computeConnectionCost(selectedDevices));
			communicationAccumulator.add(computeCommunicationCost(locations));
			
			//-----------------------------------Greedy-MSC-----------------------------------
			for (Device device : devices.values()) {
				device.clearLocationsResponsibleFor();
				device.setAssociatedMEC(ConfigurationMEC.get(device));
			}
			//System.out.println("G-MSC selects");
			selectedDevices = Selection.greedyMSC(devices, locations);
			//System.out.println("G-MSC selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			Adjustment.processingMecDetermination(selectedDevices, locations, FloydWarshall);
			GMSCaccumulator.add(computeTotalCost(selectedDevices, locations));
			
			//-----------------------------------ESR-----------------------------------
			for (Device device : devices.values()) {
				device.clearLocationsResponsibleFor();
				device.setAssociatedMEC(ConfigurationMEC.get(device));
			}
			//System.out.println("ESR selects");
			selectedDevices = Selection.ESR(devices, locations);
			//System.out.println("ESR selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			Adjustment.processingMecDetermination(selectedDevices, locations, FloydWarshall);
			ESRaccumulator.add(computeTotalCost(selectedDevices, locations));
			
			
		} // end for
		
		// print result
		System.out.println(name + " Our = " + accumulator.mean());
		//System.out.println(name + " G-MSC = " + GMSCaccumulator.mean());
		//System.out.println(name + " ESR = " + ESRaccumulator.mean());
		
		System.out.println(name + " Connection = " + connectionAccumulator.mean());
		//System.out.println(name + " Communication = " + communicationAccumulator.mean());
	
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
	
	public double computeConnectionCost(Set<Device> selectedDevices) {
		double consumed = 0;
		for (Device device : selectedDevices) {
			consumed += device.getConnectionEnergy().get(device.getAssociatedMEC());
		}
		return consumed;
	}
	
	public double computeCommunicationCost(Locations locations) {
		double consumed = 0;
		for (Location location : locations.values()) {
			consumed += location.getCommunicationEnergy();
		}
		return consumed;
	}
	
	public double computeMeasuredPower(double emittedPower, double distance) {
		final double physicalSize = 2.0;
		if (distance < physicalSize) {
			return emittedPower;
		} else {
			return emittedPower / Math.pow((distance / physicalSize), 2.0);
		}
	}
	
	public double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2.0) + Math.pow((y1 - y2), 2.0));
	}

}
