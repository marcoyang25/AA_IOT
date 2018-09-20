package device;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

public class Location {
	private int id; // unique ID
	private static int count = 0;
	private double x; // x coordinate
	private double y; // y coordinate
	
	boolean satisfied;
	private Group selectedGroup;
	private Set<Group> groups;
	private Set<Device> coveredBy;

	public Location() {
		this.id = ++count;
		
		// initialize x and y coordinate
		final Random radius = new Random();
		final Random angle = new Random();
		double r = Device.MAX_RADIUS * radius.nextDouble();
		double a = 2 * Math.PI * angle.nextDouble();
		this.x = r * Math.cos(a);
		this.y = r * Math.sin(a);
		
		this.selectedGroup = null;
		this.groups = new HashSet<>();
		this.coveredBy = new HashSet<>();
	}
	
	public void addGroup(Group group) {
		this.groups.add(group);
	}
	
	public Set<Device> getCoveredBy() {
		return Collections.unmodifiableSet(coveredBy);
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
		if (!(obj instanceof Location))
			return false;
		Location other = (Location) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("Location [id=%s, covered by:%s]", id, coveredBy.size());
	}
	
}
