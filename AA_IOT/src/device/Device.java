package device;

import java.util.Set;
import java.util.Map;

import com.google.common.collect.Range;

public class Device {
	private int id; // unique id
	private static int count = 0;
	private double x; // x coordinate
	private double y; // y coordinate
	private double cost; // energy cost
	private double connectionCost;
	private double communicationCost;
	private Set<Location> coverage; // locations covered by device

	private double precision;
	private Map<Location, Double> accuracies;
	private Map<Location, Range> intervals;

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
		if (!(obj instanceof Device))
			return false;
		Device other = (Device) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Device [id=%s, coverage:%s]", id, coverage.size());
	}

}
