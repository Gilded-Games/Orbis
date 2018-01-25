package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RotationHelp;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class to make testing easier.
 * @author Emile
 *
 */
public abstract class PathwayProblemBase implements ISearchProblem<PathwayNode>
{

	protected final BlockPos start, end;

	protected final List<BlueprintData> pieces;

	public PathwayProblemBase(BlockPos start, BlockPos end, List<BlueprintData> pieces)
	{
		this.start = start;
		this.end = end;
		this.pieces = pieces;
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

	protected abstract boolean isSuccessor(PathwayNode node, PathwayNode parent);

	@Override
	public boolean isGoal(PathwayNode state)
	{
		return this.end.getX() >= state.getMin().getX() && this.end.getX() <= state.getMax().getX()
				&& this.end.getY() >= state.getMin().getY() && this.end.getY() <= state.getMax().getY()
				&& this.end.getZ() >= state.getMin().getZ() && this.end.getZ() <= state.getMax().getZ();
	}

	@Override
	public abstract double heuristic(PathwayNode state);

	@Override
	public abstract double costBetween(PathwayNode parent, PathwayNode child);

	@Override
	public abstract boolean shouldTerminate(PathwayNode currentState);

	@Override
	public abstract boolean contains(Collection<PathwayNode> visitedStates, PathwayNode currentState);

}
