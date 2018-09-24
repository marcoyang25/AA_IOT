package device;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;

import graph.Topo;
import graph.Vertex;

public class Adjustment {
	private Adjustment() {
	}

	public static void adjust(Set<Device> selectedDevices, Locations locations, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		Queue<Device> Q = new LinkedList<>();
		Set<Device> unadjustedDevices = new HashSet<>(selectedDevices);

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
			while (!unadjustedDevices.isEmpty()) {
				Device maxEnergyDevice = findMaxEnergyDevice(unadjustedDevices, f);
				Q.offer(maxEnergyDevice);
				while (!Q.isEmpty()) {
					Device deviceToAdjust = Q.poll();
					unadjustedDevices.remove(deviceToAdjust);
					// adjust this device

				} // end while
			} // end while
		} // end for

	} // end method adjust

	private static void setMinCommnicationEnergyMEC(Location location, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		Vertex MinCommnicationEnergyMEC = null;
		Double minEnergy = Double.POSITIVE_INFINITY;
		for (Vertex mec : mecs) {
			Double CommnicationEnergy = locationCommnicationEnergy(location, mec, f);
			if (CommnicationEnergy <= minEnergy) {
				minEnergy = CommnicationEnergy;
				MinCommnicationEnergyMEC = mec;
			}
		}
		location.setCommunicationEnergy(minEnergy);
		location.setProcessingMEC(MinCommnicationEnergyMEC);
	} // end method setMinCommnicationEnergyMEC

	private static double locationCommnicationEnergy(Location location, Vertex mec,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double consumed = 0;
		for (Device device : location.getSelectedGroup().getMembers()) {
			consumed += Topo.getEnergyConsumed(device.getAssociatedMEC(), mec, f);
		}
		return consumed;
	} // end method locationCommnicationEnergy

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
			if ((connectionEnergy + communicationEngergy) <= max) {
				max = connectionEnergy + communicationEngergy;
				MaxEnergyDevice = device;
			}
		}
		return MaxEnergyDevice;
	} // end method findMaxEnergyDevice

}
