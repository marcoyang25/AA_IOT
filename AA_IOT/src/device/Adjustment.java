package device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;

import graph.Topo;
import graph.Vertex;

public class Adjustment {
	private Adjustment() {
	}

	public static double adjust(Set<Device> selectedDevices, Locations locations, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double totalRecuced = 0;
		// for each location and for each device in its selected group members
		// set, add the location to the device LocationsResponsibleFor
		for (Location location : locations.values()) {
			for (Device device : location.getSelectedGroup().getMembers()) {
				device.addLocationResponsibleFor(location);
			}
		}
		// calculate processing MEC for each location
		for (Location location : locations.values()) {
			setMinCommnicationEnergyMEC(location, mecs, f);
		}

		// running n times
		for (int n = 0; n < 1; n++) {
			Queue<Device> Q = new LinkedList<>();
			Set<Device> unadjustedDevices = new HashSet<>(selectedDevices);
			while (!unadjustedDevices.isEmpty()) {
				Device maxEnergyDevice = findMaxEnergyDevice(unadjustedDevices, f);
				Q.offer(maxEnergyDevice);
				while (!Q.isEmpty()) {
					Device deviceToAdjust = Q.poll();
					unadjustedDevices.remove(deviceToAdjust);
					// adjust this device
					double reducedEnergy = adjustDevice(deviceToAdjust, mecs, f);
					totalRecuced += reducedEnergy;
					// System.out.println("reducedEnergy: " +
					// reducedEnergy);
					addAffectedDevicesByRelation(Q, deviceToAdjust, unadjustedDevices);
				} // end while
			} // end while
		} // end for
		return totalRecuced;
	} // end method adjust

	/**
	 * Find the MEC server with minimum energy to process the data of an input
	 * location. Then, for this location, set its processing MEC and
	 * communication energy.
	 */
	private static void setMinCommnicationEnergyMEC(Location location, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		Vertex MinCommnicationEnergyMEC = null;
		double minEnergy = Double.POSITIVE_INFINITY;
		for (Vertex mec : mecs) {
			double CommnicationEnergy = locationCommnicationEnergy(location, mec, f);
			if (CommnicationEnergy <= minEnergy) {
				minEnergy = CommnicationEnergy;
				MinCommnicationEnergyMEC = mec;
			}
		}
		location.setCommunicationEnergy(minEnergy);
		location.setProcessingMEC(MinCommnicationEnergyMEC);
	} // end method setMinCommnicationEnergyMEC

	/**
	 * Calculate the communication energy to process the data of a location. The
	 * data is sent to the input MEC server.
	 */
	private static double locationCommnicationEnergy(Location location, Vertex mec,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double consumed = 0;
		for (Device device : location.getSelectedGroup().getMembers()) {
			consumed += Topo.getEnergyConsumed(device.getAssociatedMEC(), mec, f);
		}
		return consumed;
	} // end method locationCommnicationEnergy

	/**
	 * Calculate the communication energy for an input device.
	 */
	private static double deviceCommunicationEnergy(Device device, FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double consumed = 0;
		for (Location location : device.getLocationsResponsibleFor()) {
			consumed += Topo.getEnergyConsumed(device.getAssociatedMEC(), location.getProcessingMEC(), f);
		}
		return consumed;
	} // end method deviceCommunicationEnergy

	private static Device findMaxEnergyDevice(Set<Device> unadjustedDevices,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		Device MaxEnergyDevice = null;
		double max = Double.NEGATIVE_INFINITY;
		for (Device device : unadjustedDevices) {
			double connectionEnergy = device.getConnectionEnergy().get(device.getAssociatedMEC());
			double communicationEngergy = deviceCommunicationEnergy(device, f);
			if ((connectionEnergy + communicationEngergy) >= max) {
				max = connectionEnergy + communicationEngergy;
				MaxEnergyDevice = device;
			}
		}
		return MaxEnergyDevice;
	} // end method findMaxEnergyDevice

	/**
	 * Adjust the device by connecting the device to a MEC server which reduces
	 * maximum overall energy cost. The processing MECs and communication energy
	 * for locations are set under this circumstance.
	 */
	private static double adjustDevice(Device deviceToAdjust, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		final Vertex originalAssociatedMEC = deviceToAdjust.getAssociatedMEC();
		final double originalEnergy = deviceToAdjust.getConnectionEnergy().get(deviceToAdjust.getAssociatedMEC())
				+ locationsResponsibleForCommunicationEnergy(deviceToAdjust.getLocationsResponsibleFor());
		Vertex maxReducedMEC = null;
		double minEnergy = Double.POSITIVE_INFINITY;
		for (Vertex candidateMEC : deviceToAdjust.getServingMECs()) {
			// skip the original associated MEC server
			if (originalAssociatedMEC.equals(candidateMEC)) {
				continue;
			}
			if (!candidateMEC.hasCapacity()) {
				continue;
			}
			// change its associated MEC
			deviceToAdjust.setAssociatedMEC(candidateMEC);
			// compute the connection energy
			double connectionEnergy = deviceToAdjust.getConnectionEnergy().get(candidateMEC);
			double communicationEnergy = 0;
			for (Location location : deviceToAdjust.getLocationsResponsibleFor()) {
				communicationEnergy += MinCommnicationEnergyForLocation(location, mecs, f);
			}
			// only adjust when the energy can be reduced (< originalEnergy)
			if ((connectionEnergy + communicationEnergy < originalEnergy)
					&& (connectionEnergy + communicationEnergy < minEnergy)) {
				minEnergy = connectionEnergy + communicationEnergy;
				maxReducedMEC = candidateMEC;
			}
		} // end for

		// if the total energy cannot be reduced
		// no adjustment is made
		if (maxReducedMEC == null) {
			deviceToAdjust.setAssociatedMEC(originalAssociatedMEC);
			return 0;
		}
		// the total energy can be reduced
		else {
			deviceToAdjust.setAssociatedMEC(maxReducedMEC);
			// setting the serving status of MEC servers
			originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
			maxReducedMEC.setServing(maxReducedMEC.getServing() + 1);
			// adjust locations responsible for
			for (Location location : deviceToAdjust.getLocationsResponsibleFor()) {
				setMinCommnicationEnergyMEC(location, mecs, f);
			}
			return originalEnergy - minEnergy;
		}
	} // end method adjustDevice

	/**
	 * Calculate minimum energy to process the data of an input location.
	 */
	private static double MinCommnicationEnergyForLocation(Location location, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double minEnergy = Double.POSITIVE_INFINITY;
		for (Vertex mec : mecs) {
			double CommnicationEnergy = locationCommnicationEnergy(location, mec, f);
			if (CommnicationEnergy <= minEnergy) {
				minEnergy = CommnicationEnergy;
			}
		}
		return minEnergy;
	} // end method MinCommnicationEnergyForLocation

	private static void addAffectedDevices(Queue<Device> Q, Device deviceToAdjust, Set<Device> unadjustedDevices) {
		for (Location location : deviceToAdjust.getLocationsResponsibleFor()) {
			for (Device affected : location.getSelectedGroup().getMembers()) {
				if (deviceToAdjust.equals(affected)) {
					continue;
				}
				if (unadjustedDevices.contains(affected) && !Q.contains(affected)) {
					Q.offer(affected);
				}
			}
		}
	} // end method addAffectedDevices

	private static void addAffectedDevicesByRelation(Queue<Device> Q, Device deviceToAdjust,
			Set<Device> unadjustedDevices) {
		Map<Device, Integer> affectedDevices = new HashMap<>();
		for (Location location : deviceToAdjust.getLocationsResponsibleFor()) {
			for (Device affected : location.getSelectedGroup().getMembers()) {
				if (deviceToAdjust.equals(affected)) {
					continue;
				}
				if (unadjustedDevices.contains(affected) && !Q.contains(affected)) {
					Integer count = affectedDevices.get(affected);
					if (count == null) {
						affectedDevices.put(affected, 1);
					} else {
						affectedDevices.put(affected, count + 1);
					}
				}
			} // end for
		} // end for

		// sort the map
		List<Map.Entry<Device, Integer>> sorted = new ArrayList<>(affectedDevices.entrySet());
		sorted.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));
		// add to Q
		for (Entry<Device, Integer> entry : sorted) {
			Q.offer(entry.getKey());
		}
	} // end method addAffectedDevicesByRelation

	private static double locationsResponsibleForCommunicationEnergy(Set<Location> locationsResponsibleFor) {
		double energy = 0;
		for (Location location : locationsResponsibleFor) {
			energy += location.getCommunicationEnergy();
		}
		return energy;
	} // end method locationsResponsibleForCommunicationEnergy

	public static double processingMecDetermination(Set<Device> selectedDevices, Locations locations,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		// for each location and for each device in its selected group members
		// set, add the location to the device LocationsResponsibleFor
		for (Location location : locations.values()) {
			for (Device device : location.getSelectedGroup().getMembers()) {
				device.addLocationResponsibleFor(location);
			}
		}
		// first calculate processing MEC for each location
		// and compute total energy consumption before adjustments
		for (Location location : locations.values()) {
			setMinCommnicationEnergyMEC(location, f);
		}
		double originalEnergy = computeTotalCost(selectedDevices, locations);

		// making adjustments
		// connect each device to MEC that minimizes connection energy cost
		sequentialConnect(selectedDevices);
		// calculate processing MEC for each location
		for (Location location : locations.values()) {
			setMinCommnicationEnergyMEC(location, f);
		}
		double adjustedEnergy = computeTotalCost(selectedDevices, locations);
		return originalEnergy - adjustedEnergy;
	} // end method processingMecDetermination

	private static void setMinCommnicationEnergyMEC(Location location,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		Vertex MinCommnicationEnergyMEC = null;
		double minEnergy = Double.POSITIVE_INFINITY;
		List<Vertex> mecs = new ArrayList<>();
		for (Device device : location.getSelectedGroup().getMembers()) {
			mecs.add(device.getAssociatedMEC());
		}
		for (Vertex mec : mecs) {
			double CommnicationEnergy = locationCommnicationEnergy(location, mec, f);
			if (CommnicationEnergy <= minEnergy) {
				minEnergy = CommnicationEnergy;
				MinCommnicationEnergyMEC = mec;
			}
		}
		location.setCommunicationEnergy(minEnergy);
		location.setProcessingMEC(MinCommnicationEnergyMEC);
	} // end method setMinCommnicationEnergyMEC

	public static void sequentialConnect(Set<Device> selectedDevices) {
		for (Device device : selectedDevices) {
			final Vertex originalAssociatedMEC = device.getAssociatedMEC();
			for (Vertex candidateMEC : device.getServingMECs()) {
				if (candidateMEC.hasCapacity()) {
					device.setAssociatedMEC(candidateMEC);
					originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
					candidateMEC.setServing(candidateMEC.getServing() + 1);
					break;
				}
			}
		}
	} // end method sequentialConnect

	private static void connectToMinConnectionEnergyMEC(Set<Device> selectedDevices) {
		for (Device device : selectedDevices) {
			final Vertex originalAssociatedMEC = device.getAssociatedMEC();
			Vertex minMEC = null;
			double minEnergy = Double.POSITIVE_INFINITY;
			for (Vertex candidateMEC : device.getServingMECs()) {
				double connectionEnergy = device.getConnectionEnergy().get(candidateMEC);
				if (connectionEnergy <= minEnergy) {
					minEnergy = connectionEnergy;
					minMEC = candidateMEC;
				}
			}
			device.setAssociatedMEC(minMEC);
			originalAssociatedMEC.setServing(originalAssociatedMEC.getServing() - 1);
			minMEC.setServing(minMEC.getServing() + 1);
		}
	} // end connectToMinConnectionEnergyMEC

	private static double computeTotalCost(Set<Device> selectedDevices, Locations locations) {
		double consumed = 0;
		for (Device device : selectedDevices) {
			consumed += device.getConnectionEnergy().get(device.getAssociatedMEC());
		}
		for (Location location : locations.values()) {
			consumed += location.getCommunicationEnergy();
			consumed += Network2.PROCESSING_ENERGY;
		}
		return consumed;
	} // end method computeTotalCost

}
