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
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class Network3 implements Runnable {
	private String name;
	private File file;
	private int DEVICES_SIZE = 1200;
	private int LOCATIONS_SIZE = 1000;
	private int MEC_NUM = 20;
	private int MEC_CAPACITY = 130;
	private double BASE_ALPHA;
	private double BASE_GAMMA;
	private double ALPHA;
	private double GAMMA;
	public static final double PROCESSING_ENERGY = 0.1;
	
	public Network3(String name, String pathname, int DEVICES_SIZE, int LOCATIONS_SIZE, int MEC_NUM, double BASE_ALPHA,
			double BASE_GAMMA, double ALPHA, double GAMMA) {
		this.name = name;
		this.file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(pathname)).getFile());
		this.DEVICES_SIZE = DEVICES_SIZE;
		this.LOCATIONS_SIZE = LOCATIONS_SIZE;
		this.MEC_NUM = MEC_NUM;
		this.BASE_ALPHA = BASE_ALPHA;
		this.BASE_GAMMA = BASE_GAMMA;
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
		StatsAccumulator EDMSreducedAccumulator = new StatsAccumulator();
		StatsAccumulator GMSCreducedAccumulator = new StatsAccumulator();
		StatsAccumulator ESRreducedAccumulator = new StatsAccumulator();
		
		// running n times
		for (int n = 0; n < 20; n++) {
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
				for(Vertex mec : vertices.mec) {
					//d.getConnectionEnergy().put(mec, Math.random() * 0.8 + 0.2);		
					//d.getConnectionEnergy().put(mec, 0.2);
					d.getConnectionEnergy().put(mec, 0.2 * Math.pow(calculateDistance(d.getX(), d.getY(), mec.getX(), mec.getY()), 2));
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
						l.setEmittedPower(emittedPower);
						double measuredPower = computeMeasuredPower(emittedPower, distance);
						double accuracy = Math.abs(emittedPower - measuredPower);
						d.getAccuracies().put(l, accuracy);
						d.getIntervals().put(l, Range.closed(measuredPower - d.getPrecision() / 2,
								measuredPower + d.getPrecision() / 2));
					}
				} // end for
				
				// construct groups for location l
				Locations.constructGroups(l, BASE_ALPHA, BASE_GAMMA);
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
			
			// -----------------------------Creating New Input--------------------------------

			//------------------------------Creating New Devices------------------------------
			/*for (int i = 0; i < 1600; i++) {
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
				for(Vertex mec : vertices.mec) {
					d.getConnectionEnergy().put(mec, 0.2 * Math.pow(calculateDistance(d.getX(), d.getY(), mec.getX(), mec.getY()), 2));
					//d.getConnectionEnergy().put(mec, Math.random() * 0.8 + 0.2);
				}
				for (Location l : locations.values()) {
					double distance = calculateDistance(d.getX(), d.getY(), l.getX(), l.getY());
					if (distance > 2 && distance <= Device.MAX_DIS_BETWEEN) {
						l.addCoveredby(d);
						d.addCoverage(l);
						// TODO convert distance to accuracy
						final double emittedPower = l.getEmittedPower();
						double measuredPower = computeMeasuredPower(emittedPower, distance);
						double accuracy = Math.abs(emittedPower - measuredPower);
						d.getAccuracies().put(l, accuracy);
						d.getIntervals().put(l, Range.closed(measuredPower - d.getPrecision() / 2,
								measuredPower + d.getPrecision() / 2));
					}
				}		
			}*/ // end for


			// building groups again
			for (Location l : locations.values()) {
				l.clearGroups();
				Locations.constructGroups(l, ALPHA, GAMMA);
			}

			//--------------------------------------------------------------------------------
			
			
			
			//-----------------------------------Our1-----------------------------------	
			//System.out.println("devicesSelection selects");
			Set<Device> selectedDevices = Selection.devicesSelection(devices, locations);
			//System.out.println("devicesSelection selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			
			// compute the number of selected devices
			EDMSreducedAccumulator.add(selectedDevices.size());
			
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			//EDMSreducedAccumulator.add(Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall));
			Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall);
			accumulator.add(computeTotalCost(selectedDevices, locations));
			
			/*for (Vertex mec : vertices.mec) {
				System.out.printf("%.5f\n", (double) mec.getServing() / (double) MEC_CAPACITY);
			}
			System.out.println("--------------------------------------------------");*/
			
			/*for (Location location : locations.values()) {
				System.out.println(location.getSelectedGroup().getMembers().size());
			}*/
			//connectionAccumulator.add(computeConnectionCost(selectedDevices));
			//communicationAccumulator.add(computeCommunicationCost(locations));
			
			//System.out.println(computeConnectionCost(selectedDevices));
			//System.out.println(computeCommunicationCost(locations));
			
			//------------------------------Creating New Devices------------------------------
			for (int i = 0; i < 800; i++) {
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
				for(Vertex mec : vertices.mec) {
					d.getConnectionEnergy().put(mec, 0.2 * Math.pow(calculateDistance(d.getX(), d.getY(), mec.getX(), mec.getY()), 2));
					//d.getConnectionEnergy().put(mec, Math.random() * 0.8 + 0.2);
				}
				for (Location l : locations.values()) {
					double distance = calculateDistance(d.getX(), d.getY(), l.getX(), l.getY());
					if (distance > 2 && distance <= Device.MAX_DIS_BETWEEN) {
						l.addCoveredby(d);
						d.addCoverage(l);
						// TODO convert distance to accuracy
						final double emittedPower = l.getEmittedPower();
						double measuredPower = computeMeasuredPower(emittedPower, distance);
						double accuracy = Math.abs(emittedPower - measuredPower);
						d.getAccuracies().put(l, accuracy);
						d.getIntervals().put(l, Range.closed(measuredPower - d.getPrecision() / 2,
								measuredPower + d.getPrecision() / 2));
					}
				}		
			} // end for


			// building groups again
			for (Location l : locations.values()) {
				l.clearGroups();
				Locations.constructGroups(l, ALPHA, GAMMA);
			}

			//--------------------------------------------------------------------------------
			
			//-----------------------------------Our2-----------------------------------	
			//System.out.println("devicesSelection selects");
			for (Vertex mec : vertices.mec) {
				mec.setServing(0);
			}
			for (Device device : devices.values()) {
				device.clearLocationsResponsibleFor();
				Vertex mec = ConfigurationMEC.get(device);
				device.setAssociatedMEC(mec);
				mec.setServing(mec.getServing() + 1);
			}
			selectedDevices = Selection.devicesSelection(devices, locations);
			//System.out.println("devicesSelection selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			
			// compute the number of selected devices
			EDMSreducedAccumulator.add(selectedDevices.size());
			
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			//EDMSreducedAccumulator.add(Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall));
			Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall);
			GMSCaccumulator.add(computeTotalCost(selectedDevices, locations));
			
			//------------------------------Creating New Devices------------------------------
			/*for (int i = 0; i < 200; i++) {
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
				for(Vertex mec : vertices.mec) {
					d.getConnectionEnergy().put(mec, 0.2 * Math.pow(calculateDistance(d.getX(), d.getY(), mec.getX(), mec.getY()), 2));
					//d.getConnectionEnergy().put(mec, Math.random() * 0.8 + 0.2);
				}
				for (Location l : locations.values()) {
					double distance = calculateDistance(d.getX(), d.getY(), l.getX(), l.getY());
					if (distance > 2 && distance <= Device.MAX_DIS_BETWEEN) {
						l.addCoveredby(d);
						d.addCoverage(l);
						// TODO convert distance to accuracy
						final double emittedPower = l.getEmittedPower();
						double measuredPower = computeMeasuredPower(emittedPower, distance);
						double accuracy = Math.abs(emittedPower - measuredPower);
						d.getAccuracies().put(l, accuracy);
						d.getIntervals().put(l, Range.closed(measuredPower - d.getPrecision() / 2,
								measuredPower + d.getPrecision() / 2));
					}
				}		
			} // end for


			// building groups again
			for (Location l : locations.values()) {
				l.clearGroups();
				Locations.constructGroups(l, ALPHA, GAMMA);
			}*/

			//--------------------------------------------------------------------------------
			
			//-----------------------------------Our3-----------------------------------	
			//System.out.println("devicesSelection selects");
			/*for (Vertex mec : vertices.mec) {
				mec.setServing(0);
			}
			for (Device device : devices.values()) {
				device.clearLocationsResponsibleFor();
				Vertex mec = ConfigurationMEC.get(device);
				device.setAssociatedMEC(mec);
				mec.setServing(mec.getServing() + 1);
			}
			selectedDevices = Selection.devicesSelection(devices, locations);
			//System.out.println("devicesSelection selectedDevices: " + selectedDevices.size());
			// for each unselected device, set its associated MEC to null
			
			// compute the number of selected devices
			EDMSreducedAccumulator.add(selectedDevices.size());
			
			for(Device unselected : Sets.difference(new HashSet<>(devices.values()), selectedDevices)) {
				Vertex originalAssociatedMEC = unselected.getAssociatedMEC();
				originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
				unselected.setAssociatedMEC(null);
			}
			//EDMSreducedAccumulator.add(Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall));
			Adjustment.adjust(selectedDevices, locations, vertices.mec, FloydWarshall);
			accumulator.add(computeTotalCost(selectedDevices, locations));
			System.out.println(computeTotalCost(selectedDevices, locations));*/
			
			
		} // end for
		
		// print result
		System.out.println(name + " Our = " + accumulator.mean());
		System.out.println(name + " G-MSC = " + GMSCaccumulator.mean());
		/*System.out.println(name + " ESR = " + ESRaccumulator.mean());
		
		//System.out.println(name + " Connection = " + connectionAccumulator.mean());
		//System.out.println(name + " Communication = " + communicationAccumulator.mean());
		
		System.out.println(name + " Our reduces = " + EDMSreducedAccumulator.mean());
		System.out.println(name + " G-MSC reduces = " + GMSCreducedAccumulator.mean());
		System.out.println(name + " ESR reduces = " + ESRreducedAccumulator.mean());*/
	
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