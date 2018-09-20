package device;

import java.util.HashSet;
import java.util.Set;
import com.google.common.collect.Range;
import com.google.common.math.StatsAccumulator;

public class Group {
	private int id; // unique ID
	private static int count = 0;
	private Range<Double> interval;
	private Set<Device> members; // the members of the group

	public Group() {
		this.id = ++count;
		this.interval = null;
		this.members = new HashSet<>();
	}

	public Range<Double> getInterval() {
		return interval;
	}

	public void setInterval(Range<Double> interval) {
		this.interval = interval;
	}

	public void addMember(Device device) {
		this.members.add(device);
	}

	/**
	 * Calculate the average accuracy of the group measuring the location by
	 * StatsAccumulator
	 */
	public double getAverageAccuracy(Location location) {
		StatsAccumulator s = new StatsAccumulator();
		for (Device device : members) {
			s.add(device.getAccuracies().get(location));
		}
		return s.mean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Group))
			return false;
		Group other = (Group) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Group [members:%s]", id, members);
	}

}
