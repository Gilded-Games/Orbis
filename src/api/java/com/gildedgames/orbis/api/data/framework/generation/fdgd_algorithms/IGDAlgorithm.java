package com.gildedgames.orbis.api.data.framework.generation.fdgd_algorithms;

import com.gildedgames.orbis.api.data.framework.FrameworkAlgorithm;
import com.gildedgames.orbis.api.data.framework.FrameworkType;
import com.gildedgames.orbis.api.data.framework.Graph;
import com.gildedgames.orbis.api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;

import java.util.Random;

/**
 * Implements a general framework for creating algorithms that draw a graph.
 * This allows for easier experimentation with different algorithms.
 */
public interface IGDAlgorithm
{
	void initialize(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random);

	/**
	 * This method computes the forces on the nodes and the edges.
	 * @param graph
	 * @param type
	 */
	void step(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random, int fdgdIterations);

	/**
	 * Returns true if the graph is in equilibrium and the algorithm is finished.
	 * @param graph
	 * @param type
	 * @return
	 */
	FrameworkAlgorithm.Phase inEquilibrium(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations);

	void resetOnSpiderweb(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations);
}
