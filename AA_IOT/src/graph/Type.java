package graph;

public enum Type {
	BS("Base Station"), CLOUDSERVER("Cloud Server"), MEC("MEC Server"), SWITCH("Switch");

	private final String value;

	private Type(String value) {
		this.value = value;
	}

	public String toString() {
		return value;
	}

}
