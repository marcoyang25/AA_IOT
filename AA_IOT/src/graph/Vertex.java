package graph;

import java.util.Set;

public class Vertex {
	public static final double BS_ENERGY = 0.5;
	public static final double CLOUDSEVER_ENERGY = 0.1;
	public static final double MEC_ENERGY = 0.1;
	public static final double MEC_FORWARDING_ENERGY = 0.5;
	public static final double SWITCH_ENERGY = 0.5;
	public static final int CS_ID = 1; // cloud server id
	// aggregating multiple flows into a single one and compressing with a ratio
	public static final double RATIO = 0.5;

	private int id;
	private Type type;

	public Vertex(int id, Type type) {
		this.id = id;
		this.type = type;
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
