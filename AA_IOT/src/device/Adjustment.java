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

		// calculate processing MEC for each location
		for (Location location : locations.values()) {
			setMinCommnicationEnergyMEC(location, mecs, f);
		}

		for (int n = 0; n < 1; n++) {
			while (!unadjustedDevices.isEmpty()) {

			} // end while
		} // end for

	} // end method adjust

	public static void setMinCommnicationEnergyMEC(Location location, List<Vertex> mecs,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		Vertex MinCommnicationEnergyMEC = null;
		Double minEnergy = Double.POSITIVE_INFINITY;
		for (Vertex mec : mecs) {
			Double CommnicationEnergy = LocationCommnicationEnergy(location, mec, f);
			if (CommnicationEnergy <= minEnergy) {
				minEnergy = CommnicationEnergy;
				MinCommnicationEnergyMEC = mec;
			}
		}
		location.setCommunicationEnergy(minEnergy);
		location.setProcessingMEC(MinCommnicationEnergyMEC);
	}

	public static double LocationCommnicationEnergy(Location location, Vertex mec,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double consumed = 0;
		for (Device device : location.getSelectedGroup().getMembers()) {
			consumed += Topo.getEnergyConsumed(device.getAssociatedMEC(), mec, f);
		}
		return consumed;
	} // end method LocationCommnicationEnergy

}
