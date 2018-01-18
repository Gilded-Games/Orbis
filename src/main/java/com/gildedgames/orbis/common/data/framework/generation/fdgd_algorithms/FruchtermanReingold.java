package com.gildedgames.orbis.common.data.framework.generation.fdgd_algorithms;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.data.framework.FrameworkAlgorithm;
import com.gildedgames.orbis.common.data.framework.FrameworkType;
import com.gildedgames.orbis.common.data.framework.Graph;
import com.gildedgames.orbis.common.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.common.data.framework.generation.FDGDNode;

import java.util.Random;

public class FruchtermanReingold implements IGDAlgorithm
{

	private static int UN_MAX_ITERATIONS = 300;

	private static int UP_MAX_ITERATIONS = 1500;

	private static float END_SPEED = 3f;

	private static float START_SPEED = 35;

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


	@Override
	public void initialize(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random)
	{
		this.max_iterations = UN_MAX_ITERATIONS +
				(int)(((UP_MAX_ITERATIONS - UN_MAX_ITERATIONS)/100f) * Math.min(100, graph.vertexSet().size()));

		this.W = (float) graph.vertexSet().stream().mapToDouble(FDGDNode::getWidth ).sum();
		this.L = (float) graph.vertexSet().stream().mapToDouble(FDGDNode::getLength).sum();
		this.area = this.W * this.L;
		this.k = (float) Math.sqrt(this.area / graph.vertexSet().size());
		this.s = START_SPEED;

		this.cooling = (float) Math.pow(END_SPEED / this.s, 1.0 / this.max_iterations);
		OrbisCore.LOGGER.info(this.cooling);
		OrbisCore.LOGGER.info(this.max_iterations);
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
					float dx = v.getX() - u.getX();
					float dz = v.getZ() - u.getZ();
					float dist = this.euclid(dx, 0, dz);
					float rep = this.fr(dist);
					if (dist > 0f)
//						v.addForce((dx / dist) * rep, 0, (dz / dist) * rep);
						v.addForce(Math.signum(dx) * this.fr(Math.abs(dx)), 0, Math.signum(dz) * this.fr(Math.abs(dz)));
				}
			}
		}
		for (FDGDEdge e : graph.edgeSet())
		{
			float dx = e.node1().getX() - e.node2().getX();
			float dz = e.node1().getZ() - e.node2().getZ();
			float dist = this.euclid(dx, 0, dz);
			float atr = this.fa(dist);
//			float fx = (dx / dist) * atr;
//			float fz = (dz / dist) * atr;
			float fx = Math.signum(dx) * this.fa(Math.abs(dx));
			float fz = Math.signum(dz) * this.fa(Math.abs(dz));
			if(dist > 0f)
				e.node1().subtrForce(fx, 0, fz);
				e.node2().addForce(fx, 0, fz);
		}
		for (FDGDNode v : graph.vertexSet())
		{
			float str_f = this.euclid(v.getForceX(), 0, v.getForceZ());
			if(str_f > 0)
			{
//				float newX = v.getX() + (v.getForceX() / str_f) * Math.min(v.getForceX(), this.s);
//				float newZ = v.getZ() + (v.getForceZ() / str_f) * Math.min(v.getForceZ(), this.s);
				float newX = v.getX() + Math.signum(v.getForceX()) * Math.min(Math.abs(v.getForceX()), this.s);
				float newZ = v.getZ() + Math.signum(v.getForceZ()) * Math.min(Math.abs(v.getForceZ()), this.s);
				float random_border = random.nextFloat() * 0.01f;
				newX = Math.min(this.W / 2f + random_border, Math.max(-this.W / 2f - random_border, newX));
				newZ = Math.min(this.L / 2f + random_border, Math.max(-this.L / 2f - random_border, newZ));
				v.setPosition(newX, v.getY(), newZ);
			}
		}
		this.s *= this.cooling;
	}

	@Override
	public FrameworkAlgorithm.Phase inEquilibrium(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations)
	{
		if (fdgdIterations > this.max_iterations)
		{
			OrbisCore.LOGGER.info("END OF FDGD");
			return FrameworkAlgorithm.Phase.PATHWAYS;
		}
		return FrameworkAlgorithm.Phase.FDGD;
	}
}
