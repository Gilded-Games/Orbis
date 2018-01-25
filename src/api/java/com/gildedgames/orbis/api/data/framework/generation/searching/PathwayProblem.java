package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RegionHelp;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.List;

public class PathwayProblem extends PathwayProblemBase
{

	private double maxLength;

	private final Collection<BlueprintRegion> fragments;

	private final IRegion boundingBox;

	private final BlueprintRegion startFragment;

	private static final float pathwaysBoundingBox = 8;

	public PathwayProblem(BlockPos start, FDGDNode startFragment, BlockPos end, List<BlueprintData> pieces, Collection<BlueprintRegion> fragments)
	{
		super(start, end, pieces);

		for (BlueprintData b : pieces)
		{
			this.maxLength = Math.max(this.maxLength, b.getWidth() * b.getWidth() + b.getHeight() + b.getLength() * b.getLength());
		}
		this.maxLength = Math.sqrt(this.maxLength);

		this.fragments = fragments;

		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = 0, maxY = 0, maxZ = 0;

		for (BlueprintRegion fragment : fragments)
		{
			minX = Math.min(minX, fragment.getMin().getX());
			minY = Math.min(minY, fragment.getMin().getY());
			minZ = Math.min(minZ, fragment.getMin().getZ());

			maxX = Math.max(maxX, fragment.getMax().getX());
			maxY = Math.max(maxY, fragment.getMax().getY());
			maxZ = Math.max(maxZ, fragment.getMax().getZ());
		}

		this.startFragment = new BlueprintRegion(startFragment.getRegionForBlueprint().getMin(), startFragment.getRotation(), startFragment.getData());

		this.boundingBox = RegionHelp.expand(new Region(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)), (int) (this.maxLength * pathwaysBoundingBox));
	}

	@Override
	public PathwayNode start()
	{
		return new PathwayNode(null, this.startFragment, this.start);
	}

	@Override
	protected boolean isSuccessor(PathwayNode node, PathwayNode parent)
	{
		if (this.isGoal(node))
			return true;

		for (BlueprintRegion fragment : this.fragments)
		{
			if (RegionHelp.intersects(fragment, node))
			{
				return false;
			}
		}

		for (PathwayNode s : parent.fullPath())
		{
			if (RegionHelp.intersects(node, s))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public double heuristic(PathwayNode state)
	{
		if (this.isGoal(state))
		{
			return 0;
		}
		BlockPos exit = state.endConnection;
		return Math.abs(this.end.getX() - exit.getX()) + Math.abs(this.end.getY() - exit.getY()) + Math.abs(this.end.getZ() - exit.getZ());
	}

	@Override
	public double costBetween(PathwayNode parent, PathwayNode child)
	{
		BlockPos exit = child.endConnection;
		if (this.isGoal(child))
		{
			return Math.abs(this.end.getX() - exit.getX()) + Math.abs(this.end.getY() - exit.getY()) + Math.abs(this.end.getZ() - exit.getZ());
		}
		return child.getWidth() + child.getHeight() + child.getLength();//TODO: Why was this not using the exit again, lol?
	}

	@Override
	public boolean shouldTerminate(PathwayNode currentState)
	{
		return !RegionHelp.intersects(currentState, this.boundingBox);
	}

	@Override
	public boolean contains(Collection<PathwayNode> visitedStates, PathwayNode currentState)
	{
		//TODO: Kinda forgot what this did
		for (PathwayNode visitedState : visitedStates)
		{
			// Returns true if we have visited the  of the current state contains
//			if (visitedState != currentState.parent && RegionHelp.contains(visitedState, currentState.endConnection))
//			{
//				return true;
//			}
		}
		return false;
	}

}
