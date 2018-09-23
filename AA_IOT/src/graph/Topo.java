package graph;

import java.util.*;

import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.ext.*;
import org.jgrapht.graph.*;

public class Topo {
	private Topo() {
	}

	public static double getEnergyConsumed(Vertex source, Vertex sink,
			FloydWarshallShortestPaths<Vertex, DefaultEdge> f) {
		double consumed = 0;
		if (f == null) {
			return -1;
		}
		if (source.equals(sink)) {
			return 0;
		} else {
			consumed = f.getPathWeight(source, sink) * Vertex.SWITCH_ENERGY;
		}
		return consumed;
	} // end method getEnergyConsumed

	public static GraphImporter<Vertex, DefaultEdge> createImporter() {
		// create vertex provider
		VertexProvider<Vertex> vertexProvider = new VertexProvider<Vertex>() {
			@Override
			public Vertex buildVertex(String id, Map<String, String> attributes) {
				Vertex v = new Vertex(Integer.valueOf(id), Type.SWITCH);
				return v;
			}
		};

		// create edge provider
		EdgeProvider<Vertex, DefaultEdge> edgeProvider = new EdgeProvider<Vertex, DefaultEdge>() {
			@Override
			public DefaultEdge buildEdge(Vertex from, Vertex to, String label, Map<String, String> attributes) {
				return new DefaultEdge();
			}
		};

		// create GML importer
		GmlImporter<Vertex, DefaultEdge> importer = new GmlImporter<>(vertexProvider, edgeProvider);

		return importer;
	} // end method GraphImporter

}
