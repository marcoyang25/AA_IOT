package device;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import graph.Vertex;

public class Location {
	private int id; // unique ID
	private static int count = 0;
	private double x; // x coordinate
	private double y; // y coordinate

	boolean satisfied;
	private Group selectedGroup;
	private Set<Group> groups;
	private Vertex processingMEC;
	private double communicationEnergy;
	private Set<Device> coveredBy;
	private double emittedPower;

	public Location() {
		this.id = ++count;

		// initialize x and y coordinate
		double r = Device.MAX_RADIUS * Math.random();
		double a = 2 * Math.PI * Math.random();
		this.x = r * Math.cos(a);
		this.y = r * Math.sin(a);

		this.satisfied = false;
		this.selectedGroup = null;
		this.groups = new HashSet<>();
		this.processingMEC = null;
		this.communicationEnergy = -1;
		this.coveredBy = new HashSet<>();
		this.emittedPower = -1;
	}

	public int getId() {
		return id;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public boolean isSatisfied() {
		return satisfied;
	}

	public void setSatisfied(boolean satisfied) {
		this.satisfied = satisfied;
	}

	public Group getSelectedGroup() {
		return selectedGroup;
	}

	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	public void addGroup(Group group) {
		this.groups.add(group);
	}

	public Set<Group> getGroups() {
		return Collections.unmodifiableSet(groups);
	}

	public void clearGroups() {
		this.groups.clear();
	}

	public int getGroupsSize() {
		return this.groups.size();
	}

	public Vertex getProcessingMEC() {
		return processingMEC;
	}

	public void setProcessingMEC(Vertex processingMEC) {
		this.processingMEC = processingMEC;
	}

	public double getCommunicationEnergy() {
		return communicationEnergy;
	}

	public void setCommunicationEnergy(double communicationEnergy) {
		this.communicationEnergy = communicationEnergy;
	}

	public void addCoveredby(Device device) {
		this.coveredBy.add(device);
	}

	public Set<Device> getCoveredBy() {
		return Collections.unmodifiableSet(coveredBy);
	}

	public double getEmittedPower() {
		return emittedPower;
	}

	public void setEmittedPower(double emittedPower) {
		this.emittedPower = emittedPower;
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
