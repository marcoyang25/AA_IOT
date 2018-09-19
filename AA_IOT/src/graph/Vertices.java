package graph;

import java.util.*;

public class Vertices {
	public Map<Integer, Vertex> idToVertex;
	public List<Vertex> bs;
	public List<Vertex> mec;
	public List<Vertex> switches;
	
	public Vertices() {
		idToVertex = new HashMap<>();
		bs = new ArrayList<>();
		mec = new ArrayList<>();
		switches = new ArrayList<>();
	}
	
}
