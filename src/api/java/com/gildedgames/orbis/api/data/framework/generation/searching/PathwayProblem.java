package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.framework.generation.FDGDNode;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.RotationHelp;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PathwayProblem implements ISearchProblem<PathwayNode>
{

	private double maxLength;

	private final Collection<BlueprintRegion> fragments;

	private final IRegion boundingBox;

	private final BlueprintRegion startFragment;

	private static final float pathwaysBoundingBox = 8;

	protected final BlockPos start, end;

	protected final List<BlueprintData> pieces;

	public PathwayProblem(BlockPos start, FDGDNode startFragment, BlockPos end, List<BlueprintData> pieces, Collection<BlueprintRegion> fragments)
	{
		this.start = start;
		this.end = end;
		this.pieces = pieces;

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
	public List<PathwayNode> successors(PathwayNode parentState)
	{
		List<PathwayNode> successors = new ArrayList<>();

		BlockPos currentPosition = parentState.endConnection;

		EnumFacing lastSide = parentState.sideOfConnection();
		EnumFacing toConnect = lastSide.getOpposite();

		boolean skipToConnectTest = parentState.parent == null;

		currentPosition = PathwayUtil.adjacent(currentPosition, lastSide);

		for (BlueprintData blueprint : this.pieces)
		{
			Region rect = new Region(new BlockPos(0, 0, 0), new BlockPos(blueprint.getWidth() - 1, blueprint.getHeight() - 1, blueprint.getLength() - 1));

			for (Entrance entrance : blueprint.entrances())
			{
				for (Entrance exit : blueprint.entrances())
				{
					if (entrance != exit)
					{
						for (Rotation rotation : Rotation.values())
						{
							BlockPos trEntrance = RotationHelp.rotate(entrance.getPos(), rect, rotation);

							IRegion trRect = RotationHelp.rotate(rect, rotation);

							EnumFacing entranceSide = PathwayUtil.sideOfConnection(trRect, trEntrance);

							if (toConnect != entranceSide && !skipToConnectTest)
								continue;

							int dx = currentPosition.getX() - trEntrance.getX();
							int dy = currentPosition.getY() - trEntrance.getY();
							int dz = currentPosition.getZ() - trEntrance.getZ();

							BlockPos trExit = RotationHelp.rotate(exit.getPos(), rect, rotation);

							BlockPos endConnection = new BlockPos(trExit.getX() + dx, trExit.getY() + dy, trExit.getZ() + dz);

							BlockPos fragmentMin = new BlockPos(dx + trRect.getMin().getX(), dy + trRect.getMin().getY(), dz + trRect.getMin().getZ());

							BlueprintRegion fragment = new BlueprintRegion(fragmentMin, rotation, blueprint);

							PathwayNode node = new PathwayNode(parentState, fragment, endConnection);

							if (this.isSuccessor(node, parentState))
							{
								successors.add(node);
							}
						}
					}
				}
			}
		}
		return successors;
	}

	@Override
	public PathwayNode start()
	{
		return new PathwayNode(null, this.startFragment, this.start);
	}

	protected boolean isSuccessor(PathwayNode node, PathwayNode parent)
	{
		if (this.isGoal(node))
			return true;

		for (BlueprintRegion fragment : this.fragments)
		{
//			if (RegionHelp.intersects(fragment, node))
//			{
//				return false;
//			}
		}

		for (PathwayNode s : parent.fullPath())
		{
//			if (RegionHelp.intersects(node, s))
//			{
//				return false;
//			}
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

	@Override
	public boolean isGoal(PathwayNode state)
	{
		return this.end.getX() >= state.getMin().getX() && this.end.getX() <= state.getMax().getX()
				&& this.end.getY() >= state.getMin().getY() && this.end.getY() <= state.getMax().getY()
				&& this.end.getZ() >= state.getMin().getZ() && this.end.getZ() <= state.getMax().getZ();
	}

}
