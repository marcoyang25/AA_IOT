package com.mnetlab.aaiot.graph;

import com.mnetlab.aaiot.device.Device;

public class Vertex {
	public static final double BS_ENERGY = 0.5;
	public static final double CLOUDSEVER_ENERGY = 0.1;
	public static final double MEC_ENERGY = 0.1;
	public static final double MEC_FORWARDING_ENERGY = 0.5;
	public static final double SWITCH_ENERGY = 0.9;
	public static final int CS_ID = 1; // cloud server id
	// aggregating multiple flows into a single one and compressing with a ratio
	public static final double RATIO = 0.5;
	private double x; // x coordinate
	private double y; // y coordinate

	private int id;
	private Type type;
	private int capacity;
	private int serving;

	public Vertex(int id, Type type) {
		this.id = id;
		this.type = type;
		this.capacity = -1;
		this.serving = -1;
	}

	public Vertex(int id, Type type, int capacity) {
		this.id = id;
		this.type = type;
		this.capacity = capacity;
		this.serving = 0;
		double r = Device.MAX_RADIUS * Math.random();
		double a = 2 * Math.PI * Math.random();
		this.x = r * Math.cos(a);
		this.y = r * Math.sin(a);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public int getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public double getCapacity() {
		return capacity;
	}

	public boolean hasCapacity() {
		if (serving < capacity) {
			return true;
		} else {
			return false;
		}
	}

	public int getServing() {
		return serving;
	}

	public void setServing(int serving) {
		if (serving <= capacity) {
			this.serving = serving;
		}
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
		if (!(obj instanceof Vertex))
			return false;
		Vertex other = (Vertex) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("(%s, %s)", id, type);
	}

}
