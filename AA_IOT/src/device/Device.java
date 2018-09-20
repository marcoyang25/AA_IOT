package device;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import com.google.common.collect.Range;

public class Device {
	public static final double MAX_RADIUS = 100;
	public static final double MAX_DIS_BETWEEN = 12; // maximum distance between

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
	private Map<Location, Range<Double>> intervals;

	public Device() {
		this.id = ++count;

		final Random radius = new Random();
		final Random angle = new Random();
		// initialize x and y coordinate
		double r = Device.MAX_RADIUS * radius.nextDouble();
		double a = 2 * Math.PI * angle.nextDouble();
		this.x = r * Math.cos(a);
		this.y = r * Math.sin(a);

		this.cost = -1; // initialize cost
		this.connectionCost = -1;
		this.communicationCost = -1;
		this.coverage = new HashSet<>();

		this.precision = -1;
		this.accuracies = new HashMap<>();
		this.intervals = new HashMap<>();
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public Set<Location> getCoverage() {
		return coverage;
	}

	public double getPrecision() {
		return precision;
	}

	public Map<Location, Double> getAccuracies() {
		return accuracies;
	}

	public Map<Location, Range<Double>> getIntervals() {
		return intervals;
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
