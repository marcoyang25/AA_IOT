package device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.common.collect.Range;

public class Locations extends HashMap<Integer, Location> {
	public static void constructGroups(Location location, Double alpha, Double gamma) {
		/**
		 * Construct the possible sets of devices selection (group) for a
		 * location, which satisfies alpha and gamma
		 */
		List<Device> coveredby = new ArrayList<>(location.getCoveredBy());
		// checking the intersection of two devices
		for (int i = 0; i < coveredby.size(); i++) {
			for (int j = i; j < coveredby.size(); j++) {
				Group group = new Group();
				// device 1 and 2
				Device d1 = coveredby.get(i);
				Device d2 = coveredby.get(j);
				// the interval of device 1 and 2
				Range<Double> i1 = d1.getIntervals().get(location);
				Range<Double> i2 = d2.getIntervals().get(location);

				// if d1 == d2
				if (d1.equals(d2)) {
					Range<Double> interval = Range.closed(i1.lowerEndpoint(), i2.upperEndpoint());
					group.setInterval(interval);
					group.addMember(d1);
				}
				// if i1 overlaps i2
				else if (i1.isConnected(i2)) {
					Range<Double> interval = i1.intersection(i2);
					group.setInterval(interval);
					group.addMember(d1);
					group.addMember(d2);
				}
				// d1 doesn't overlap d2
				else {
					continue;
				}

				// if the width of the interval < gamma
				if (Math.abs(group.getInterval().upperEndpoint() - group.getInterval().lowerEndpoint()) <= gamma) {
					// if the average of the group members <= alpha
					if (group.getAverageAccuracy(location) <= alpha) {
						// add the group to location's groups
						location.addGroup(group);
						continue;
					} else {
						// the set V collects all the devices that enclose the
						// interval of the group
						Set<Device> V = new HashSet<>();
						for (Device device : coveredby) {
							Range<Double> interval = device.getIntervals().get(location);
							if (interval.encloses(group.getInterval()) && !device.equals(d1) && !device.equals(d2)) {
								V.add(device);
							}
						}
						// if the accuracy does not satisfy alpha
						// then the group is discarded after the while loop
						while (!V.isEmpty()) {
							// find the device with MAX accuracy
							double max = Double.MIN_VALUE;
							Device maxAccuracy = null;
							for (Device device : V) {
								if (device.getAccuracies().get(location) >= max) {
									max = device.getAccuracies().get(location);
									maxAccuracy = device;
								}
							}
							// add the device with maximum accuracy
							group.addMember(maxAccuracy);
							V.remove(maxAccuracy); // V = V - d
							// checking the average accuracy of the group
							if (group.getAverageAccuracy(location) <= alpha) {
								location.addGroup(group);
								break;
							}
						} // end while
					}
				} else {
					// discard group
					// not satisfying gamma
					continue;
				}

			} // end for i loop
		} // end for j loop
	} // end method constructGroups
}
