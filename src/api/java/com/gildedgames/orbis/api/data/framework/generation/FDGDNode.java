package com.gildedgames.orbis.api.data.framework.generation;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.util.RotationHelp;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.Map.Entry;

public class FDGDNode extends BlueprintRegion
{
	// The bounding box of the FDGD node is increased by toleranceDist
	// to make sure there's always room for the pathways.
	private final int toleranceDist;

	//This is the center
	private float posX, posY, posZ;

	private float prevX, prevY, prevZ;

	private float forceX, forceY, forceZ;

	private boolean isIntersection = false;

	private FDGDEdge oldEdge1, oldEdge2;

	public FDGDNode(BlueprintData data, BlockPos pos, int toleranceDist)
	{
		super(pos, data);
		this.data = data;
		this.posX = pos.getX();
		this.posY = pos.getY();
		this.posZ = pos.getZ();
		this.computeMinMax();
		this.toleranceDist = data.entrances().get(0).toConnectTo().getToleranceDist();
	}

	public FDGDNode(BlueprintData intersection, BlockPos pos, FDGDEdge oldEdge1, FDGDEdge oldEdge2, int toleranceDist)
	{
		this(intersection, pos, toleranceDist);
		this.isIntersection = true;
		this.oldEdge1 = oldEdge1;
		this.oldEdge2 = oldEdge2;
	}

	private void computeMinMax()
	{
		final IRegion region = this.getRegionForBlueprint();
		this.min = region.getMin().subtract(new Vec3i(this.toleranceDist, this.toleranceDist, this.toleranceDist));
		this.max = region.getMax().add(new Vec3i(this.toleranceDist, this.toleranceDist, this.toleranceDist));
	}

	public float getX()
	{
		return this.posX;
	}

	public float getY()
	{
		return this.posY;
	}

	public float getZ()
	{
		return this.posZ;
	}

	public float getPrevX()
	{
		return this.prevX;
	}

	public float getPrevY()
	{
		return this.prevY;
	}

	public float getPrevZ()
	{
		return this.prevZ;
	}

	public float getForceX()
	{
		return this.forceX;
	}

	public float getForceY()
	{
		return this.forceY;
	}

	public float getForceZ()
	{
		return this.forceZ;
	}

	public void setPosition(float x, float y, float z)
	{
		this.prevX = this.posX;
		this.prevY = this.posY;
		this.prevZ = this.posZ;

		this.posX = x;
		this.posY = y;
		this.posZ = z;

		this.computeMinMax();
	}

	public void setForce(float x, float y, float z)
	{
		this.forceX = x;
		this.forceY = y;
		this.forceZ = z;
	}

	public void addForce(float x, float y, float z)
	{
		this.forceX += x;
		this.forceY += y;
		this.forceZ += z;
	}

	public void subtrForce(float x, float y, float z)
	{
		this.forceX -= x;
		this.forceY -= y;
		this.forceZ -= z;
	}

	public void applyForce()
	{
		this.setPosition(this.posX + this.forceX, this.posY + this.forceY, this.posZ + this.forceZ);
	}

	/**
	 * Try all possible ways to connect this node to its parents with the edges
	 * out of it. Also tries all different rotations to minimise the length between. 
	 *
	 * Be careful: This can change the region this node takes because it can choose
	 * a different rotation! Don't use this at the end of the Framework algorithm.
	 *
	 * This method assumes that there is a solution. First solve the Framework
	 * CSP to guarantee this (see {@link })
	 * @param edges
	 */
	public void assignConnections(Collection<FDGDEdge> edges)
	{
		int best = Integer.MAX_VALUE;
		Map<FDGDEdge, Entrance> bestResult = null;
		Rotation bestRotation = Rotation.NONE;
		final List<FDGDEdge> edgesL = new ArrayList<>(edges);
		for (final Rotation rotation : Rotation.values())
		{
			final List<Entrance> entrances = this.getEntrances(rotation);
			if (entrances.size() < edges.size())
			{
				throw new IllegalStateException();
			}
			final Tuple<Map<FDGDEdge, Entrance>, Integer> result = this.bestEntrances(edgesL, entrances, 0, 0, best);
			if (result != null)
			{
				bestResult = result.getFirst();
				best = result.getSecond();
				bestRotation = rotation;
			}
		}
		if (bestResult == null)
		{
			OrbisAPI.LOGGER.info("Was unable to find a valid assignment of entrances to edges. This should not happen.");
			OrbisAPI.LOGGER.info(best);
		}
		else
		{
			for (final Entry<FDGDEdge, Entrance> edge : bestResult.entrySet())
			{
				edge.getKey().setConnection(this, edge.getValue());
			}
			this.rotation = bestRotation;
			this.computeMinMax();
		}
	}

	/**
	 * Does the same thing as the method up here, but does not try
	 * different rotations so the node does not change in dimensions.
	 * @param edges
	 */
	public void assignConnectionsFixRot(Collection<FDGDEdge> edges)
	{
		final List<Entrance> entrances = this.getEntrances(this.rotation);
		final List<FDGDEdge> edgesL = new ArrayList<>(edges);
		if (entrances.size() < edges.size())
		{
			throw new IllegalStateException();
		}
		final Tuple<Map<FDGDEdge, Entrance>, Integer> result = this.bestEntrances(edgesL, entrances, 0, 0, Integer.MAX_VALUE);
		if (result == null)
		{
			OrbisAPI.LOGGER.info("Was unable to find a valid assignment of entrances to edges. This should not happen.");
		}
		//			throw new FailedToGenerateException("Was not able to find a suitable connection assignment.");
		else
		{
			for (final Entry<FDGDEdge, Entrance> edge : result.getFirst().entrySet())
			{
				edge.getKey().setConnection(this, edge.getValue());
			}
		}
	}

	/**
	 * A collection of assignments of edges to entrance is valid iff
	 * for each entrance e connecting to a node n, there are no two
	 * other assigned entrances e1 and e2 for which the line e1-e2
	 * intersects2D e-n.
	 * @param solution
	 * @return
	 */
	private boolean isValidConnectionAssignment(Tuple<Map<FDGDEdge, Entrance>, Integer> solution)
	{
		Map<FDGDEdge, Entrance> assignment = solution.getFirst();
		for (FDGDEdge edge : assignment.keySet())
		{
			FDGDNode n = edge.getOpposite(this);
			Entrance e = assignment.get(edge);
			for (Entrance e1 : assignment.values())
			{
				for (Entrance e2 : assignment.values())
				{
					if (e != e1 && e2 != e && e1 != e2)
					{
						float e1X = e1.getBounds().getMin().getX(), e1Z = e1.getBounds().getMin().getZ();
						float e2X = e2.getBounds().getMin().getX(), e2Z = e2.getBounds().getMin().getZ();
						float dx1 = e1X - this.getX(), dz1 = e1Z - this.getZ();
						float length1 = (float) Math.sqrt(dx1 * dx1 + dz1 * dz1);
						e1X -= dx1 * 0.01 / length1;
						e1Z -= dz1 * 0.01 / length1;
						float dx2 = e2X - this.getX(), dz2 = e2Z - this.getZ();
						float length2 = (float) Math.sqrt(dx2 * dx2 + dz2 * dz2);
						e2X -= dx2 * 0.01 / length2;
						e2Z -= dz2 * 0.01 / length2;
						if (FDGenUtil.isIntersecting(n.getX(), n.getZ(), e.getBounds().getMin().getX(), e.getBounds().getMin().getZ(), // Edge n-e
								e1X, e1Z, e2X, e2Z, false)) // Edge e1-e2
						{
							return false;
						}
					}
				}
			}
			for (FDGDEdge edge2 : assignment.keySet())
			{
				FDGDNode n2 = edge2.getOpposite(this);
				Entrance e2 = assignment.get(edge2);
				if (edge != edge2 && FDGenUtil.isIntersecting(n.getX(), n.getZ(), e.getBounds().getMin().getX(), e.getBounds().getMin().getZ(),
						n2.getX(), n2.getZ(), e2.getBounds().getMin().getX(), e2.getBounds().getMin().getZ(), false))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Branch and bound exhaustive search of all the possible ways to connect
	 * this blueprint to its neighbors. The shortest route is the best.
	 * Returns null if no better config was found.
	 * If a better config was found it returns a tuple with the connections chosen
	 * for each edge, and an integer representing the total distance between 
	 * the connections and the nodes they're connected to.
	 */
	private Tuple<Map<FDGDEdge, Entrance>, Integer> bestEntrances(List<FDGDEdge> edges, List<Entrance> entrancesLeft, int edgeIndex, int cost, int best)
	{
		if (edgeIndex >= edges.size())
		{
			return new Tuple<>(new HashMap<>(edges.size()), cost);
		}
		final FDGDEdge edge = edges.get(edgeIndex);
		Tuple<Map<FDGDEdge, Entrance>, Integer> bestInDepth = null;
		final FDGDNode opposite = edge.getOpposite(this);
		final Map<Entrance, Integer> costMap = new HashMap<>(entrancesLeft.size());
		for (final Entrance entrance : entrancesLeft)
		{
			costMap.put(entrance,
					cost + FDGenUtil.euclidian(entrance.getBounds().getMin(), (int) opposite.getX(), (int) opposite.getY(), (int) opposite.getZ()));
		}

		//TODO: See if the heuristic has noticeable performance improvements
		entrancesLeft.sort(Comparator.comparing(costMap::get));
		for (final Entrance entrance : entrancesLeft)
		{
			if (!edge.pathway().equals(entrance.toConnectTo()))
			{
				continue;
			}
			final int newCost = costMap.get(entrance);
			//Prune branch if we already found a solution with a lower total cost
			if (newCost >= best)
			{
				continue;
			}
			final List<Entrance> copy = new ArrayList<>(entrancesLeft);
			copy.remove(entrance);

			//Go into recursion
			final Tuple<Map<FDGDEdge, Entrance>, Integer> result = this.bestEntrances(edges, copy, edgeIndex + 1, newCost, best);
			if (result != null)
			{
				result.getFirst().put(edge, entrance);
				if (this.isValidConnectionAssignment(result))
				{
					bestInDepth = result;
					best = result.getSecond();
				}
				else
				{
					result.getFirst().remove(edge);
				}
			}
		}
		return bestInDepth;
	}

	public List<Entrance> getEntrances(Rotation rotation)
	{
		return RotationHelp.getEntrances(this.getData(), rotation, this.centerAsBP());
	}

	public BlockPos centerAsBP()
	{
		return new BlockPos((int) this.posX, (int) this.posY, (int) this.posZ);
	}

	public boolean isIntersection()
	{
		return this.isIntersection;
	}

	public BlueprintRegion getRegionForBlueprint()
	{
		IRegion r = RotationHelp.regionFromCenter((int) this.posX, (int) this.posY, (int) this.posZ, this.data, this.rotation);
		return new BlueprintRegion(r.getMin(), this.rotation, this.data);
	}
}
