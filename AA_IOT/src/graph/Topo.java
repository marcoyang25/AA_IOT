package graph;

import java.util.*;

import org.jgrapht.*;
import org.jgrapht.ext.*;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.*;

public class Topo {
	private static final Random random = new Random();

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

	public static double getMinAggregationEnergyConsumed(Vertex bs, List<Vertex> mecs, Vertex cloud, double size,
			DijkstraShortestPath<Vertex, DefaultEdge> d) {
		double min = Double.MAX_VALUE;
		double energyConsumed;
		// calculate for each MEC
		for (Vertex mec : mecs) {
			energyConsumed = getAggregationEnergyConsumed(bs, mec, cloud, size, d);
			if (energyConsumed <= min) {
				min = energyConsumed;
			}
		}
		return min;
	} // end method getMinAggregationEnergyConsumed

	/*
	 * calculates the energy consumption of aggregation path from the serving BS
	 * to the cloud server D by computing the energy consumption of paths from
	 * BS to the MEC server v and from v to D
	 */
	private static double getAggregationEnergyConsumed(Vertex bs, Vertex mec, Vertex cloud, double size,
			DijkstraShortestPath<Vertex, DefaultEdge> d) {
		double consumed = 0;
		if (d == null)
			return -1;
		consumed += (Topo.getEnergyConsumed(bs, mec, size, d) - Vertex.MEC_FORWARDING_ENERGY + Vertex.MEC_ENERGY);
		// flow size becomes size * (1 - Vertex.RATIO) after traversing the MEC
		consumed += Topo.getEnergyConsumed(mec, cloud, size * (1 - Vertex.RATIO), d);
		return consumed;
	} // end method getAggregationEnergyConsumed

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
