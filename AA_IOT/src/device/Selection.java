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
			Location locationToSatisfy = findMaxGroupsLocation(unsatisfiedLocations);
			Group selectedGroup = null;
			// l is satisfied
			locationToSatisfy.setSatisfied(true);
			unsatisfiedLocations.remove(locationToSatisfy);
			satisfiedLocations.add(locationToSatisfy);

			if ((selectedGroup = selectMaxTotalSatisfy(locationToSatisfy, unsatisfiedLocations,
					selectedDevices)) != null) {
				locationToSatisfy.setSelectedGroup(selectedGroup);
				satisfyUnsatisfiedLocations(selectedGroup, unsatisfiedLocations, satisfiedLocations, selectedDevices);
			} else if ((selectedGroup = selectMaxTotalInvolve(locationToSatisfy, unsatisfiedLocations,
					selectedDevices)) != null) {
				locationToSatisfy.setSelectedGroup(selectedGroup);
			} else if ((selectedGroup = selectMinEnergy(locationToSatisfy, selectedDevices)) != null) {
				locationToSatisfy.setSelectedGroup(selectedGroup);
			} else {
				System.err.println("No Group is selected!!");
			}

			// Z = Z ∪ g
			selectedDevices.addAll(selectedGroup.getMembers());

			// adjustment
			adjust(satisfiedLocations, selectedDevices);

			// Z = the union of selected groups for all satisfied locations
			// devices that are not in use are deleted
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

	private static Group selectMaxTotalSatisfy(Location locationToSatisfy, Set<Location> unsatisfiedLocations,
			Set<Device> selectedDevices) {
		Group maxGroup = null;
		double max = 0;
		for (Group selecting : locationToSatisfy.getGroups()) {
			// not satisfying any location which is originally unsatisfied
			if (totalSatisfy(selecting, unsatisfiedLocations, selectedDevices) == 0) {
				continue;
			} else {
				double energy = computeDevicesEnergy(Sets.difference(selecting.getMembers(),
						Sets.intersection(selecting.getMembers(), selectedDevices)));
				if ((totalSatisfy(selecting, unsatisfiedLocations, selectedDevices) / energy) >= max) {
					max = totalSatisfy(selecting, unsatisfiedLocations, selectedDevices) / energy;
					maxGroup = selecting;
				}
			}
		}
		return maxGroup;
	} // end method selectMaxTotalSatisfy

	/**
	 * To satisfy other locations after satisfying MaxGroupsLocation
	 */
	private static void satisfyUnsatisfiedLocations(Group selected, Set<Location> unsatisfiedLocations,
			Set<Location> satisfiedLocations, Set<Device> selectedDevices) {
		Set<Location> locationsSelectedGroupCovers = new HashSet<>();
		// the union of locations that each member in the group covers
		for (Device member : selected.getMembers()) {
			locationsSelectedGroupCovers.addAll(member.getCoverage());
		}
		for (Location location : Sets.intersection(unsatisfiedLocations, locationsSelectedGroupCovers)) {
			for (Group unsatisfiedGroup : location.getGroups()) {
				if (isSatisfy(selected, unsatisfiedGroup, selectedDevices) == 1) {
					location.setSatisfied(true);
					unsatisfiedLocations.remove(location);
					satisfiedLocations.add(location);
					// the location is now satisfied by selecting this group
					location.setSelectedGroup(unsatisfiedGroup);
					break;
				}
			}
		}
	} // end method satisfyUnsatisfiedLocations

	private static int totalSatisfy(Group selecting, Set<Location> unsatisfiedLocations, Set<Device> selectedDevices) {
		int totalSatisfy = 0;
		Set<Location> locationsSelectingGroupCovers = new HashSet<>();
		// the union of locations that each member in the group covers
		for (Device member : selecting.getMembers()) {
			locationsSelectingGroupCovers.addAll(member.getCoverage());
		}
		for (Location unsatisfiedLocation : Sets.intersection(unsatisfiedLocations, locationsSelectingGroupCovers)) {
			int satisfy = 0;
			for (Group unsatisfied : unsatisfiedLocation.getGroups()) {
				satisfy += isSatisfy(selecting, unsatisfied, selectedDevices);
			}
			satisfy = Integer.min(1, satisfy);
			totalSatisfy += satisfy;
		}
		return totalSatisfy;
	} // end method totalSatisfy

	private static Group selectMaxTotalInvolve(Location locationToSatisfy, Set<Location> unsatisfiedLocations,
			Set<Device> selectedDevices) {
		Group maxGroup = null;
		double max = 0;
		for (Group selecting : locationToSatisfy.getGroups()) {
			if (totalInvolve(selecting, unsatisfiedLocations, selectedDevices) == 0) {
				continue;
			} else {
				double energy = computeDevicesEnergy(Sets.difference(selecting.getMembers(),
						Sets.intersection(selecting.getMembers(), selectedDevices)));
				if ((totalInvolve(selecting, unsatisfiedLocations, selectedDevices) / energy) >= max) {
					max = totalInvolve(selecting, unsatisfiedLocations, selectedDevices) / energy;
					maxGroup = selecting;
				}
			}
		}
		return maxGroup;
	} // end method selectMaxTotalInvolve

	private static int totalInvolve(Group selecting, Set<Location> unsatisfiedLocations, Set<Device> selectedDevices) {
		int totalInvolve = 0;
		Set<Location> locationsSelectingGroupCovers = new HashSet<>();
		// the union of locations that each member in the group covers
		for (Device member : selecting.getMembers()) {
			locationsSelectingGroupCovers.addAll(member.getCoverage());
		}
		for (Location unsatisfiedLocation : Sets.intersection(unsatisfiedLocations, locationsSelectingGroupCovers)) {
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

	private static Group selectMinEnergy(Location locationToSatisfy, Set<Device> selectedDevices) {
		Group minGroup = null;
		double min = Double.POSITIVE_INFINITY;
		for (Group selecting : locationToSatisfy.getGroups()) {
			double energy = computeDevicesEnergy(Sets.difference(selecting.getMembers(),
					Sets.intersection(selecting.getMembers(), selectedDevices)));
			if (energy <= min) {
				min = energy;
				minGroup = selecting;
			}
		}
		return minGroup;
	} // end method selectMinEnergy

	/**
	 * The energy of connecting a device to its default associated MEC is
	 * calculated. Then, total energy consumed by input devices is returned.
	 */
	private static double computeDevicesEnergy(Set<Device> devices) {
		double energy = 0;
		for (Device device : devices) {
			energy += device.getConnectionEnergy().get(device.getAssociatedMEC());
		}
		return energy;
	} // end method computeDevicesEnergy

	private static void adjust(Set<Location> satisfiedLocations, Set<Device> selectedDevices) {
		for (Location location : satisfiedLocations) {
			final Group selectedGroup = location.getSelectedGroup();
			double currentEnergy = computeDevicesEnergy(selectedGroup.getMembers());
			for (Group candidateGroup : location.getGroups()) {
				if (selectedGroup.equals(candidateGroup)) {
					continue;
				}
				if (selectedDevices.containsAll(candidateGroup.getMembers())
						&& computeDevicesEnergy(candidateGroup.getMembers()) < currentEnergy) {
					currentEnergy = computeDevicesEnergy(candidateGroup.getMembers());
					location.setSelectedGroup(candidateGroup);
				}
			}
		}
	} // end method adjust

}
