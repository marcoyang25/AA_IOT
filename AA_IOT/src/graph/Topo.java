package graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;

public class Topo {
	private Topo() {
	}

	public static double getEnergyConsumed(Vertex source, Vertex sink, double size,
			DijkstraShortestPath<Vertex, DefaultEdge> d) {
		double consumed = 0;
		if (d == null)
			return -1;
		for (Vertex v : d.getPath(source, sink).getVertexList()) {
			switch (v.getType()) {
			case BS:
				consumed += Vertex.BS_ENERGY * size;
				break;
			case CLOUDSERVER:
				consumed += Vertex.CLOUDSEVER_ENERGY;
				break;
			case MEC:
				consumed += Vertex.MEC_FORWARDING_ENERGY * size;
				break;
			case SWITCH:
				consumed += Vertex.SWITCH_ENERGY * size;
				break;
			}
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
