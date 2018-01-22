package com.gildedgames.orbis.api.data.framework.generation.fdgd_algorithms;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.data.framework.FrameworkAlgorithm;
import com.gildedgames.orbis.api.data.framework.FrameworkType;
import com.gildedgames.orbis.api.data.framework.Graph;
import com.gildedgames.orbis.api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.api.data.framework.generation.FDGenUtil;

import java.util.Random;

public class FruchtermanReingold implements IGDAlgorithm
{

	private static int UN_MAX_ITERATIONS = 70;

	private static int UP_MAX_ITERATIONS = 1500;

//	private static int UN_SPIDER_MAX_ITERATIONS = 15;
//
//	private static int UP_SPIDER_MAX_ITERATIONS = 200;

	private static int UN_SPIDER_MAX_ITERATIONS = 35;

	private static int UP_SPIDER_MAX_ITERATIONS = 170;

	private static float END_SPEED = 22f;

	private static float MIN_START_SPEED = 32;

	private static float MAX_START_SPEED = 805;

//	private static float END_SPEED_SPIDER = 18f;
//
//	private static float MIN_START_SPEED_SPIDER = 27;
//
//	private static float MAX_START_SPEED_SPIDER = 40;

	private static float END_SPEED_SPIDER = 20f;

	private static float MIN_START_SPEED_SPIDER = 34;

	private static float MAX_START_SPEED_SPIDER = 120;

	private static float ESCAPE_MODIFIER = 1.002f;

	private static float AREA_MODIFIER = 1.2f;

	private static float C = 0.06f;

	private static float BOUNCE_MOD = 1f;

	private float cooling;

	private int max_iterations;

	private float area, k, s, W, L;

	private float fr(float x)
	{
		return (this.k * this.k) / x;
	}

	private float fa(float x)
	{
		return (x * x) / this.k;
	}

	private float euclid(float dx, float dy, float dz)
	{
		return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}


	private void initialParams(Graph<FDGDNode, FDGDEdge> graph)
	{
		this.W = (float) graph.vertexSet().stream().mapToDouble(FDGDNode::getWidth ).sum();
		this.L = (float) graph.vertexSet().stream().mapToDouble(FDGDNode::getLength).sum();
		this.area = AREA_MODIFIER * this.W * this.L;
		this.k = C * (float) Math.sqrt(this.area / graph.vertexSet().size());
	}

	@Override
	public void initialize(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random)
	{
		this.initialParams(graph);
		this.s = MIN_START_SPEED +
				(int)(((MAX_START_SPEED - MIN_START_SPEED)/100f) * Math.min(100, graph.vertexSet().size()));
		this.max_iterations = UN_MAX_ITERATIONS +
				(int)(((UP_MAX_ITERATIONS - UN_MAX_ITERATIONS)/150f) * Math.min(150, graph.vertexSet().size()));
		this.cooling = (float) Math.pow(END_SPEED / this.s, 1.0 / this.max_iterations);
		OrbisAPI.LOGGER.info(this.cooling);
		OrbisAPI.LOGGER.info(this.max_iterations);
		for (FDGDNode u : graph.vertexSet())
			u.setPosition(random.nextFloat() * this.W - this.W / 2f, 0, random.nextFloat() * this.L - this.L / 2f);
	}

	@Override
	public void step(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random, int fdgdIterations)
	{
		for (FDGDNode v : graph.vertexSet())
		{
			v.setForce(0, 0, 0);
			for (FDGDNode u : graph.vertexSet())
			{
				if(u != v)
				{
					float[] uPos = FDGenUtil.pointOfForce(v, u);
					float dx = v.getX() - uPos[0];
					float dz = v.getZ() - uPos[2];
					float dist = this.euclid(dx, 0, dz);
					if(dist > 0.01)
					{
						float repr = this.fr(dist) / dist;
						v.addForce(
								dx * repr,
								0,
								dz * repr
						);
					}
				}
			}
		}
		for (FDGDEdge e : graph.edgeSet())
		{
			float dx = e.node1().getX() - e.node2().getX();
			float dz = e.node1().getZ() - e.node2().getZ();
			float dist = this.euclid(dx, 0, dz);
			if(dist > 0.01)
			{
				float atr = this.fa(dist) / dist;
				float fx = dx * atr;
				float fz = dz * atr;
				e.node1().subtrForce(fx, 0, fz);
				e.node2().addForce(fx, 0, fz);
			}
		}
		for (FDGDNode v : graph.vertexSet())
		{
			float str_f = this.euclid(v.getForceX(), 0, v.getForceZ());
			if(str_f > 0)
			{
				float prop_f = Math.min(str_f, this.s) / str_f;
				float newX = v.getX() + v.getForceX() * prop_f;
				float newZ = v.getZ() + v.getForceZ() * prop_f;
				float random_border = random.nextFloat() * 0.01f;
				newX = Math.min(this.W / 2f + random_border, Math.max(-this.W / 2f - random_border, newX));
				newZ = Math.min(this.L / 2f + random_border, Math.max(-this.L / 2f - random_border, newZ));
				v.setPosition(newX, v.getY(), newZ);
			}
		}
		if (fdgdIterations < this.max_iterations)
			this.s *= this.cooling;
	}

	@Override
	public FrameworkAlgorithm.Phase inEquilibrium(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations)
	{
		if (fdgdIterations > this.max_iterations)
		{
			if(FDGenUtil.hasCollision(graph))
			{
				OrbisAPI.LOGGER.info("INCREASING REPULSION");
				this.k *= ESCAPE_MODIFIER; // After the max iterations, increase the repulsive force until there are no collisions.
//				this.s /= this.cooling;
			}
			else
			{
				OrbisAPI.LOGGER.info(this.k);
				OrbisAPI.LOGGER.info("END OF FDGD");
				return FrameworkAlgorithm.Phase.PATHWAYS;
			}
		}
		return FrameworkAlgorithm.Phase.FDGD;
	}

	@Override
	public void resetOnSpiderweb(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations)
	{
		this.initialParams(graph);
		this.s = MIN_START_SPEED_SPIDER +
				(int)(((MAX_START_SPEED_SPIDER - MIN_START_SPEED_SPIDER)/100f) * Math.min(100, graph.vertexSet().size()));
		int addedIterations = UN_SPIDER_MAX_ITERATIONS +
				(int)(((UP_SPIDER_MAX_ITERATIONS - UN_SPIDER_MAX_ITERATIONS)/150f) * Math.min(150, graph.vertexSet().size()));
		this.max_iterations = fdgdIterations + addedIterations;
		this.cooling = (float) Math.pow(END_SPEED_SPIDER / this.s, 1.0 / addedIterations);
		OrbisAPI.LOGGER.info(this.cooling);
		OrbisAPI.LOGGER.info(this.max_iterations);
	}
}
