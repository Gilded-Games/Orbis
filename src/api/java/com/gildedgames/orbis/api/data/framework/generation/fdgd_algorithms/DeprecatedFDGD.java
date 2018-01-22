package com.gildedgames.orbis.api.data.framework.generation.fdgd_algorithms;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.data.framework.FrameworkAlgorithm;
import com.gildedgames.orbis.api.data.framework.FrameworkType;
import com.gildedgames.orbis.api.data.framework.Graph;
import com.gildedgames.orbis.api.data.framework.generation.FDGDEdge;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.api.data.framework.generation.FDGenUtil;
import net.minecraft.util.math.MathHelper;

import java.util.Random;
import java.util.Set;
public class DeprecatedFDGD implements IGDAlgorithm
{

	private FrameworkParams params;

	private ComputedParamFac paramFac = new ComputedParamFac();

	private boolean escapePhase = false;

	@Override
	public void initialize(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random)
	{
		this.params = this.paramFac.createParams(graph, type);
		OrbisAPI.LOGGER.info(this.params);
	}

	@Override
	public void step(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, Random random, int iterations)
	{
		final float repulsion = this.params.repulsion();
		final float stiffness = this.params.stiffness();
		final float naturalLength = this.params.naturalLength();
		final int nodeDistance = this.params.nodeDistance();
		final float collisionEsc = this.params.collisionEscape();
		final float maxForce = 1000;
		final float c = this.params.C();
		for (final FDGDNode v : graph.vertexSet())
		{
			float forceX = 0, forceY = 0, forceZ = 0;

			if (!this.escapePhase)
			{
				final Set<FDGDEdge> adjacentEdges = graph.edgesOf(v);
				for (final FDGDEdge edge : adjacentEdges)
				{
					final FDGDNode u = edge.getOpposite(v);
					final float dx = edge.xOf(u) - edge.xOf(v);
					final float dz = edge.zOf(u) - edge.zOf(v);
					float stiffModifier;

					if (type == FrameworkType.RECTANGLES)
					{
						float duv = Math.abs(dx) + Math.abs(dz);
						if (duv == 0)
							duv = random.nextBoolean() ? 100 : -100;
						stiffModifier = stiffness * (duv - naturalLength) / duv;
					}
					else
					{
						final float dy = edge.yOf(u) - edge.yOf(v);
						float duv = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
						if (duv == 0)
							duv = random.nextBoolean() ? 100 : -100;
						stiffModifier = stiffness * (duv - naturalLength) / duv;
						forceY += stiffModifier * dy;
					}
					forceX += stiffModifier * dx;
					forceZ += stiffModifier * dz;
				}
			}

			final IRegion rect1 = RegionHelp.expand(v, nodeDistance);

			for (final FDGDNode u : graph.vertexSet())
			{
				if (u.equals(v))
				{
					continue;
				}
				final float dx = u.getX() - v.getX();
				final float dz = u.getZ() - v.getZ();
				if (type == FrameworkType.RECTANGLES)
				{
					final float duv = Math.abs(dx) + Math.abs(dz);
					float trepulsion = repulsion / (float) Math.pow(duv, 3);
					if (this.escapePhase && RegionHelp.intersects2D(rect1, RegionHelp.expand(u, nodeDistance)))
						continue;
					else if (this.escapePhase)
						trepulsion *= collisionEsc;
					forceX -= MathHelper.clamp(trepulsion * dx, -maxForce, maxForce);
					forceZ -= MathHelper.clamp(trepulsion * dz, -maxForce, maxForce);
				}
				else
				{
					final float dy = u.getY() - v.getY();
					final float duv = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
					float trepulsion = repulsion / (float) Math.pow(duv, 3);
					if (this.escapePhase && RegionHelp.intersects(rect1, RegionHelp.expand(u, nodeDistance)))
						continue;
					else if (this.escapePhase)
						trepulsion *= collisionEsc;
					forceX -= MathHelper.clamp(trepulsion * dx, -maxForce, maxForce);
					forceY -= MathHelper.clamp(trepulsion * dy, -maxForce, maxForce);
					forceZ -= MathHelper.clamp(trepulsion * dz, -maxForce, maxForce);
				}
			}

			if (Float.isNaN(forceX) || Float.isNaN(forceY) || Float.isNaN(forceZ) || Float.isInfinite(forceX) || Float.isInfinite(forceY) || Float
					.isInfinite(forceZ))
			{
				forceX = 0;
				forceY = 0;
				forceZ = 0;
			}

			v.setForce(forceX * c, forceY * c, forceZ * c);
		}

		// After computing the forces, apply them to the nodes
		graph.vertexSet().forEach(FDGDNode::applyForce);
		graph.edgeSet().forEach(FDGDEdge::applyForce);
	}

	@Override
	public FrameworkAlgorithm.Phase inEquilibrium(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations)
	{
		if (fdgdIterations < this.params.fdgdMaxIterations())
		{
			//Maximum distance between a node in the previous step and in the current one.
			float equilibriumState = 0;
			for (final FDGDNode node : graph.vertexSet())
			{
				equilibriumState = Math.max(equilibriumState, FDGenUtil.euclidian(
						node.getPrevX(), node.getPrevY(), node.getPrevZ(), node.getX(), node.getY(), node.getZ()));
			}

			final boolean inEquilibrium = this.escapePhase && equilibriumState < this.params.acceptEquilibriumEsc()
					|| equilibriumState < this.params.acceptEquilibrium();
			this.escapePhase =
					this.escapePhase || equilibriumState < this.params.toEscapeEquilibrium() || fdgdIterations > this.params.iterationsToEscape();
			return inEquilibrium ? FrameworkAlgorithm.Phase.PATHWAYS :FrameworkAlgorithm.Phase.FDGD;

		}
		return FrameworkAlgorithm.Phase.REBUILD1;
	}

	@Override
	public void resetOnSpiderweb(Graph<FDGDNode, FDGDEdge> graph, FrameworkType type, int fdgdIterations)
	{

	}
}
