package device;

import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.Sets;

public class Selection {
	private Selection() {
	}

	public static Set<Device> devicesSelection(Devices devices, Locations locations) {
		Set<Device> selectedDevices = new HashSet<>();
		Set<Location> unsatisfiedLocations = new HashSet<>(locations.values());
		Set<Location> satisfiedLocations = new HashSet<>();
		for (Location location : locations.values()) {
			location.setSatisfied(false);
		}
		while (!unsatisfiedLocations.isEmpty()) {
			// find an unsatisfied location l with maximum number of groups
			Location location = findMaxGroupsLocation(unsatisfiedLocations);
			Group selectedGroup = null;
			// l is satisfied
			location.setSatisfied(true);
			unsatisfiedLocations.remove(location);
			satisfiedLocations.add(location);

			if ((selectedGroup = selectMaxTotalSatisfy(location, unsatisfiedLocations, selectedDevices)) != null) {
				location.setSelectedGroup(selectedGroup);
			} else if ((selectedGroup = selectMaxTotalInvolve(location, unsatisfiedLocations,
					selectedDevices)) != null) {
				location.setSelectedGroup(selectedGroup);
			} else {

			}

			// Z = Z ∪ g
			selectedDevices.addAll(selectedGroup.getMembers());

			// adjustment

			// Z = the union of selected groups for all satisfied locations
			selectedDevices.clear();
			for (Location satisfiedLocation : satisfiedLocations) {
				selectedDevices.addAll(satisfiedLocation.getSelectedGroup().getMembers());
			}

		} // end while
		return selectedDevices;
	} // end method devicesSelection

	private static Location findMaxGroupsLocation(Set<Location> unsatisfiedLocations) {
		Location MaxGroupsLocation = null;
		int max = Integer.MIN_VALUE;
		for (Location location : unsatisfiedLocations) {
			if (location.getGroupsSize() >= max) {
				max = location.getGroupsSize();
				MaxGroupsLocation = location;
			}
		}
		return MaxGroupsLocation;
	} // end method findMaxGroupsLocation

	private static Group selectMaxTotalSatisfy(Location location, Set<Location> unsatisfiedLocations,
			Set<Device> selectedDevices) {
		Group maxGroup = null;
		double max = 0;
		for (Group selecting : location.getGroups()) {
			// not satisfying any location which is originally unsatisfied
			if (totalSatisfy(selecting, unsatisfiedLocations, selectedDevices) == 0) {
				continue;
			} else {
				double energy = computeDevicesCost(Sets.difference(selecting.getMembers(),
						Sets.intersection(selecting.getMembers(), selectedDevices)));
				if ((totalSatisfy(selecting, unsatisfiedLocations, selectedDevices) / energy) >= max) {
					max = totalSatisfy(selecting, unsatisfiedLocations, selectedDevices) / energy;
					maxGroup = selecting;
				}
			}
		}
		return maxGroup;
	}

	private static int totalSatisfy(Group selecting, Set<Location> unsatisfiedLocations, Set<Device> selectedDevices) {
		int totalSatisfy = 0;
		for (Location unsatisfiedLocation : unsatisfiedLocations) {
			int satisfy = 0;
			for (Group unsatisfied : unsatisfiedLocation.getGroups()) {
				satisfy += isSatisfy(selecting, unsatisfied, selectedDevices);
			}
			satisfy = Integer.min(1, satisfy);
			totalSatisfy += satisfy;
		}
		return totalSatisfy;
	} // end method totalSatisfy

	private static Group selectMaxTotalInvolve(Location location, Set<Location> unsatisfiedLocations,
			Set<Device> selectedDevices) {
		Group maxGroup = null;
		double max = 0;
		for (Group selecting : location.getGroups()) {
			if (totalInvolve(selecting, unsatisfiedLocations, selectedDevices) == 0) {
				continue;
			} else {
				double energy = computeDevicesCost(Sets.difference(selecting.getMembers(),
						Sets.intersection(selecting.getMembers(), selectedDevices)));
				if ((totalInvolve(selecting, unsatisfiedLocations, selectedDevices) / energy) >= max) {
					max = totalInvolve(selecting, unsatisfiedLocations, selectedDevices) / energy;
					maxGroup = selecting;
				}
			}
		}
		return maxGroup;
	}

	private static int totalInvolve(Group selecting, Set<Location> unsatisfiedLocations, Set<Device> selectedDevices) {
		int totalInvolve = 0;
		for (Location unsatisfiedLocation : unsatisfiedLocations) {
			for (Group unsatisfied : unsatisfiedLocation.getGroups()) {
				totalInvolve += isInvolve(selecting, unsatisfied, selectedDevices);
			}
		}
		return totalInvolve;
	} // end method totalInvolve

	private static int isSatisfy(Group selecting, Group unsatisfied, Set<Device> selectedDevices) {
		if (Sets.union(selecting.getMembers(), selectedDevices).containsAll(unsatisfied.getMembers())) {
			return 1;
		} else {
			return 0;
		}
	} // end method satisfy

	private static int isInvolve(Group selecting, Group unsatisfied, Set<Device> selectedDevices) {
		if (!Sets.intersection(unsatisfied.getMembers(),
				Sets.difference(selecting.getMembers(), Sets.intersection(selecting.getMembers(), selectedDevices)))
				.isEmpty()) {
			return 1;
		} else {
			return 0;
		}
	} // end method involve

	private static double computeDevicesCost(Set<Device> devices) {
		double cost = 0;
		for (Device device : devices) {
			cost += device.getCost();
		}
		return cost;
	} // end method computeDevicesCost
}
