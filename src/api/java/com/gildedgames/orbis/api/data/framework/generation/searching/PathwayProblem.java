package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.RotationHelp;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class PathwayProblem implements ISearchProblem<PathwayNode>
{

	private static final float pathwaysBoundingBox = 8;

	protected final BlockPos start, end;

	protected final List<BlueprintData> pieces;

	private final Collection<BlueprintRegion> fragments;

	private final IRegion boundingBox;

	private final BlueprintRegion startFragment;

	private double maxLength;

	private boolean hasVerticalEntrances, checkedForVertical;

	private Random rand;

	public PathwayProblem(BlockPos start, BlueprintRegion startFragment, BlockPos end, List<BlueprintData> pieces, Collection<BlueprintRegion> fragments)
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

		if (fragments.size() <= 0)
		{
			minX = Math.min(minX, start.getX());
			minY = Math.min(minY, start.getY());
			minZ = Math.min(minZ, start.getZ());

			maxX = Math.max(maxX, end.getX());
			maxY = Math.max(maxY, end.getY());
			maxZ = Math.max(maxZ, end.getZ());
		}

		this.startFragment = new BlueprintRegion(startFragment.getMin(), startFragment.getRotation(), startFragment.getData());

		this.boundingBox = RegionHelp
				.expand(new Region(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ)), (int) (this.maxLength * pathwaysBoundingBox));

		this.rand = new Random();
		this.rand.setSeed((long) start.getX() * 341873128712L + (long) start.getY() * 23289687541L + (long) start.getZ() * 132897987541L);
	}

	<T> List<T> shuffle(List<T> l)
	{
		List<T> ln = new ArrayList<>(l);
		Collections.shuffle(ln, this.rand);
		return ln;
	}

	public IRegion getBoundingBox()
	{
		return this.boundingBox;
	}

	@Override
	public List<PathwayNode> successors(PathwayNode parentState)
	{
		List<PathwayNode> successors = new ArrayList<>();

		IRegion end = parentState.endConnection;

		//TODO: FIND ENTRANCE/SIDE CLOSEST TO END POSITION
		EnumFacing[] lastSides = parentState.sidesOfConnection();

		for (EnumFacing lastSide : lastSides)
		{
			EnumFacing toConnect = lastSide.getOpposite();

			IRegion currentPosition = PathwayUtil.adjacent(end, lastSide);

			for (BlueprintData blueprint : this.shuffle(this.pieces))
			{
				Region rect = new Region(new BlockPos(0, 0, 0),
						new BlockPos(blueprint.getWidth() - 1, blueprint.getHeight() - 1, blueprint.getLength() - 1));

				for (Entrance entrance : this.shuffle(blueprint.entrances()))
				{
					for (Entrance exit : this.shuffle(blueprint.entrances()))
					{
						if (entrance == exit)
						{
							continue;
						}

						EnumFacing[] sidesOn = entrance.getFacings();

						for (EnumFacing sideOn : sidesOn)
						{
							Rotation rotation = Rotation.NONE;

							// TODO: Technically, all rotations are valid. Maybe randomize it? Add all possibilities?
							if (toConnect == EnumFacing.DOWN || toConnect == EnumFacing.UP
									|| sideOn == EnumFacing.DOWN || sideOn == EnumFacing.UP)
							{
								if (sideOn != toConnect)
								{
									continue;
								}
							}
							else
							{
								rotation = RotationHelp.getRotationDifference(RotationHelp.fromFacing(toConnect), RotationHelp.fromFacing(sideOn));
							}

							IRegion trEntrance = RotationHelp.rotate(entrance.getBounds(), rect, rotation);

							if (!RegionHelp.sameDim(trEntrance, parentState.endConnection) && parentState.parent != null)
							{
								continue;
							}

							IRegion trRect = RotationHelp.rotate(rect, rotation);
							EnumFacing[] actualConns = PathwayUtil.sidesOfConnection(trRect, trEntrance);

							boolean wrong = true;

							for (EnumFacing actualConn : actualConns)
							{
								if (actualConn == toConnect)
								{
									wrong = false;
								}
							}

							if (wrong)
							{
								OrbisAPI.LOGGER.info("THIS IS NOT RIGHT :(*");
							}

							int dx = currentPosition.getMin().getX() - trEntrance.getMin().getX();
							int dy = currentPosition.getMin().getY() - trEntrance.getMin().getY();
							int dz = currentPosition.getMin().getZ() - trEntrance.getMin().getZ();

							IRegion trExit = RotationHelp.rotate(exit.getBounds(), rect, rotation);

							IMutableRegion endConnection = new Region(trExit);

							RegionHelp.translate(endConnection, dx, dy, dz);

							int fx = dx + trRect.getMin().getX();
							int fy = dy + trRect.getMin().getY();
							int fz = dz + trRect.getMin().getZ();

							BlockPos fragmentMin = new BlockPos(fx, fy, fz);
							BlueprintRegion fragment = new BlueprintRegion(fragmentMin, rotation, blueprint);

							PathwayNode node = new PathwayNode(parentState, fragment, endConnection, PathwayUtil.getRotated(exit.getFacings(), rotation));

							if (!RegionHelp.contains(node, currentPosition))
							{
								OrbisAPI.LOGGER.info("ASDDF");
							}

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
		BlueprintData d = this.startFragment.getData();
		Entrance e = d.entrances().get(this.rand.nextInt(d.entrances().size()));

		Region r = new Region(e.getBounds());

		r.add(this.start.getX(), this.start.getY(), this.start.getZ());

		return new PathwayNode(null, this.startFragment, r, e.getFacings());
	}

	protected boolean isSuccessor(PathwayNode node, PathwayNode parent)
	{
		if (this.isGoal(node))
		{
			return true;
		}

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

	private boolean hasVerticalEntrances()
	{
		if (!this.checkedForVertical)
		{
			this.hasVerticalEntrances = false;

			boolean hasDown = false, hasUp = false;

			for (BlueprintData d : this.pieces)
			{
				for (Entrance e : d.entrances())
				{
					EnumFacing[] sides = e.getFacings();

					for (EnumFacing side : sides)
					{
						if (side == EnumFacing.DOWN)
						{
							hasDown = true;
						}

						if (side == EnumFacing.UP)
						{
							hasUp = true;
						}

						if (hasDown && hasUp)
						{
							this.hasVerticalEntrances = true;
							break;
						}
					}
				}
			}

			this.checkedForVertical = true;
		}

		return this.hasVerticalEntrances;
	}

	@Override
	public double heuristic(PathwayNode state)
	{
		if (this.isGoal(state))
		{
			return 0;
		}
		//		BlockPos exit = PathwayUtil.outside(state, state.endConnection);
		IRegion exit = state.endConnection;
		float h = Math.abs(this.end.getX() - exit.getMin().getX()) +
				Math.abs(this.end.getZ() - exit.getMin().getZ());
		//		OrbisAPI.LOGGER.info(h);
		//		if(state.parent != null)
		//			OrbisAPI.LOGGER.info(state.parent.getH());

		if (this.hasVerticalEntrances())
		{
			h += Math.abs(this.end.getY() - exit.getMin().getY());
		}

		return h;
	}

	@Override
	public double costBetween(PathwayNode parent, PathwayNode child)
	{
		IRegion exit = child.endConnection;
		//		OrbisAPI.LOGGER.info("-----------");
		//		OrbisAPI.LOGGER.info(parent);
		//		OrbisAPI.LOGGER.info(parent.endConnection);
		//		OrbisAPI.LOGGER.info(child);
		//		OrbisAPI.LOGGER.info(child.endConnection);
		//		OrbisAPI.LOGGER.info(this.end);
		if (this.isGoal(child))
		{
			double c = Math.abs(this.end.getX() - exit.getMin().getX()) + Math
					.abs(this.end.getZ() - exit.getMin().getZ());

			if (this.hasVerticalEntrances())
			{
				c += Math.abs(this.end.getY() - exit.getMin().getY());
			}

			return c;
		}

		IRegion entrance = parent.endConnection;

		float g = Math.abs(entrance.getMin().getX() - exit.getMin().getX()) +
				Math.abs(entrance.getMin().getZ() - exit.getMin().getZ());

		if (this.hasVerticalEntrances())
		{
			g += Math.abs(entrance.getMin().getY() - exit.getMin().getY());
		}

		//		OrbisAPI.LOGGER.info(g);
		return g;
		//		return child.getWidth() + child.getHeight() + child.getLength();//TODO: Why was this not using the exit again, lol?
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
			if (visitedState.parent != currentState.parent && RegionHelp.contains(visitedState, currentState.endConnection)
					|| visitedState.endConnection.equals(currentState.endConnection))
			//			if (visitedState.endConnection.equals(currentState.endConnection))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isGoal(PathwayNode state)
	{
		if (!this.hasVerticalEntrances())
		{
			return RegionHelp.containsIgnoreY(state, this.end);
		}

		return RegionHelp.contains(state, this.end);
	}

}
