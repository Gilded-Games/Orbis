package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.pathway.Entrance;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.util.ObjectFilter;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class OrbisRaytraceHelp
{

	public static final LocateWithPos<IShape> WORLD_OBJECT_LOCATOR = (world, pos) -> WorldObjectUtils.getIntersectingShape(world, pos);

	public static final LocateWithPos<IFrameworkNode> FRAMEWORK_NODE_LOCATOR = (world, pos) ->
	{
		IShape shape = WorldObjectUtils.getIntersectingShape(world, pos);

		if (shape instanceof Framework)
		{
			Framework framework = (Framework) shape;

			return framework.findIntersectingNode(pos);
		}

		return null;
	};

	public static final LocateWithPos<ISchedule> SCHEDULE_LOCATOR = (world, pos) ->
	{
		IShape shape = WorldObjectUtils.getIntersectingShape(world, pos);

		if (shape instanceof Blueprint)
		{
			Blueprint blueprint = (Blueprint) shape;

			return blueprint.findIntersectingSchedule(pos);
		}

		return null;
	};

	public static final LocateWithPos<Entrance> ENTRANCE_LOCATOR = (world, pos) ->
	{
		IShape shape = WorldObjectUtils.getIntersectingShape(world, pos);

		if (shape instanceof Blueprint)
		{
			Blueprint blueprint = (Blueprint) shape;

			return blueprint.findIntersectingEntrance(pos);
		}

		return null;
	};

	private static final List<Class<? extends IWorldObject>> blueprintClass;

	static
	{
		blueprintClass = new ArrayList<>(1);
		blueprintClass.add(Blueprint.class);
	}

	private static <T> T getRelevantRegionAt(World world, final List<Class<? extends IWorldObject>> dataType, final BlockPos pos,
			final Vec3d endPosition, final RaytraceAction<T> action, LocateWithPos<T> shapeLocator)
	{
		final T foundRegion = shapeLocator.getIntersectingShape(world, pos);

		final T result;

		if (foundRegion != null)
		{
			result = action.onFoundShape(foundRegion, pos, endPosition);
		}
		else
		{
			result = action.onIterateOutsideRegion(pos, endPosition);
		}

		return result;
	}

	public static double getFinalExtendedReach(final EntityPlayer player)
	{
		final BlockPos airRaytrace = raytraceNoSnapping(player);

		/*if (isSnappingToRegion(player) || isSelectingCorner(player))
		{
			final double x1 = player.posX;
			final double x2 = airRaytrace.getX();

			final double y1 = player.posY;
			final double y2 = airRaytrace.getY();

			final double z1 = player.posZ;
			final double z2 = airRaytrace.getZ();

			return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + +(z1 - z2) * (z1 - z2));
		}*/

		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		return playerOrbis.getDeveloperReach();
	}

	/**
	 * Returns the BlockPos the player is selecting with
	 * extended reach. This doesn't take into account
	 * things like snapping and corners.
	 * @return
	 */
	public static BlockPos raytraceNoSnapping(final EntityPlayer player)
	{
		final Vec3d finalVec = getFinalVec(player);

		final int x = MathHelper.floor(finalVec.x);
		final int y = Math.max(player.world.provider.getDimensionType() == WorldProviderOrbis.ORBIS ? 1 : 0, MathHelper.floor(finalVec.y));
		final int z = MathHelper.floor(finalVec.z);

		return new BlockPos(x, y, z);
	}

	public static boolean isSnappingToRegion(final EntityPlayer player, LocateWithPos shapeLocator)
	{
		final Vec3d positionVec = getPositionEyes(1.0F, player);
		final Vec3d finalVec = getFinalVec(player);

		final BlockPos pos = findSnapPos(player, blueprintClass, positionVec, finalVec, shapeLocator);

		return pos != null;
	}

	public static boolean isSelectingCorner(final EntityPlayer player)
	{
		return raytraceRegionCorners(player) != null;
	}

	public static Vec3d getPositionEyes(final float partialTicks, final Entity entity)
	{
		if (partialTicks == 1.0F)
		{
			return new Vec3d(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);
		}
		else
		{
			final double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
			final double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) entity.getEyeHeight();
			final double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
			return new Vec3d(d0, d1, d2);
		}
	}

	/**
	 * Returns the coordinate the player is currently having its cursor.
	 * Does cool stuff like snap to the borders of regions and corners.
	 * @return
	 */
	public static BlockPos raytraceWithRegionSnapping(final EntityPlayer player, LocateWithPos shapeLocator)
	{
		final Vec3d positionVec = getPositionEyes(1.0F, player);
		final Vec3d finalVec = getFinalVec(player);

		BlockPos pos = raytraceRegionsCorners(player, positionVec, finalVec);

		if (pos == null)
		{
			pos = findSnapPos(player, blueprintClass, positionVec, finalVec, shapeLocator);
		}

		return pos != null ? pos : new BlockPos(finalVec);
	}

	public static BlockPos raytraceRegionCorners(final EntityPlayer player)
	{
		final Vec3d positionVec = getPositionEyes(1.0F, player);
		final Vec3d finalVec = getFinalVec(player);

		return raytraceRegionsCorners(player, positionVec, finalVec);
	}

	public static <T> T raytraceShapes(final EntityPlayer player, final List<Class<? extends IWorldObject>> regionType, final double distance,
			final float partialTicks, final LocateWithPos<T> shapeLocator)
	{
		final Vec3d playerPos = getPositionEyes(partialTicks, player);
		final Vec3d look = player.getLook(partialTicks);
		final Vec3d focus = playerPos.addVector(look.x * distance, look.y * distance, look.z * distance);

		return raytraceRegions(player, regionType, playerPos, focus, shapeLocator);
	}

	private static Vec3d getFinalVec(final EntityPlayer player)
	{
		final Vec3d positionVec = getPositionEyes(1.0F, player);
		final Vec3d lookVec = player.getLookVec();

		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		final double reach = playerOrbis.getDeveloperReach();

		return positionVec.addVector(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
	}

	public static <T> T raytraceRegions(final EntityPlayer player, final List<Class<? extends IWorldObject>> dataType, final Vec3d currentPos,
			final Vec3d endPos,
			final LocateWithPos<T> shapeLocator)
	{
		return raytraceRegionsDo(player, dataType, currentPos, endPos, new RaytraceAction<>(), shapeLocator);
	}

	/**
	 * Does a raytrace through different regions and returns a BlockPos
	 * when it is at the regions edge and the region contains the datatype.
	 * Used for snapping the players selector to edges of BlueprintRegions
	 */
	public static <T> BlockPos findSnapPos(final EntityPlayer player, final List<Class<? extends IWorldObject>> dataType, final Vec3d currentPos,
			final Vec3d endPos, LocateWithPos<T> shapeLocator)
	{
		final T shape = raytraceRegionsDo(player, dataType, currentPos, endPos,
				new RaytraceAction<T>()
				{
					private int tries;

					private BlockPos cur;

					@Override
					public T onFoundShape(final T foundRegion, final BlockPos pos)
					{
						return null;
					}

					@Override
					public T onFoundShape(final T foundRegion, final BlockPos pos, final Vec3d endposition)
					{
						final IRegion region = ObjectFilter.cast(foundRegion, IRegion.class);

						if (region == null)
						{
							return null;
						}

						final BlockPos min = region.getMin();
						final BlockPos max = region.getMax();

						final boolean snappedMaxX = pos.getX() == max.getX() && pos.getX() <= endposition.x;
						final boolean snappedMinX = pos.getX() == min.getX() && pos.getX() >= endposition.x;

						final boolean snappedMaxY = pos.getY() == max.getY() && pos.getY() <= endposition.y;
						final boolean snappedMinY = pos.getY() == min.getY() && pos.getY() >= endposition.y;

						final boolean snappedMaxZ = pos.getZ() == max.getZ() && pos.getZ() <= endposition.z;
						final boolean snappedMinZ = pos.getZ() == min.getZ() && pos.getZ() >= endposition.z;

						if (RegionHelp.contains(region, pos) && (snappedMaxX || snappedMaxY || snappedMaxZ || snappedMinX || snappedMinY || snappedMinZ))
						{
							this.tries++;

							this.cur = pos;
						}
						return null;
					}

					@Override
					public T onIterateOutsideRegion(final BlockPos pos, final Vec3d endposition)
					{
						if (this.tries >= 1)
						{
							//TODO:
							//return new WorldRegion(this.cur, player.getEntityWorld());
						}

						return null;
					}
				}, shapeLocator);
		final IRegion region = ObjectFilter.cast(shape, IRegion.class);
		return region != null ? region.getMin() : null;
	}

	public static BlockPos raytraceRegionsCorners(final EntityPlayer player, final Vec3d currentPos, final Vec3d endPos)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		//TODO: if (!player.canInteractWithItems())
		/*{
			final IShape shape = raytraceRegionsDo(playerHook.getCurrentPower().getDataClasses(), currentPos, endPos,
					new RaytraceAction()
					{
						@Override
						public IShape onFoundShape(IShape foundRegion, BlockPos pos)
						{
							return null;
						}

						@Override
						public IShape onFoundShape(IShape foundShape, BlockPos pos, Vec3 endposition)
						{
							final IRegion region = ObjectFilter.cast(foundShape, IRegion.class);
							if (region != null && RegionHelp.isACorner(pos, region))
							{
								return new WorldRegion(pos, OrbisRaytraceHelp.playerHook.getWrapperWorld());
							}
							return null;
						}
					}, false);
			final IRegion region = ObjectFilter.cast(shape, IRegion.class);
			return region != null ? region.getMin() : null;
		}*/

		return null;
	}

	private static <T> T raytraceRegionsDo(
			final EntityPlayer player, final List<Class<? extends IWorldObject>> dataType, Vec3d currentPos, final Vec3d endPos, final RaytraceAction<T> action,
			final LocateWithPos<T> shapeLocator)
	{
		if (player == null)
		{
			return null;
		}

		if (!Double.isNaN(currentPos.x) && !Double.isNaN(currentPos.y) && !Double.isNaN(currentPos.z))
		{
			if (!Double.isNaN(endPos.x) && !Double.isNaN(endPos.y) && !Double.isNaN(endPos.z))
			{
				final int endX = MathHelper.floor(endPos.x);
				final int endY = MathHelper.floor(endPos.y);
				final int endZ = MathHelper.floor(endPos.z);
				int curX = MathHelper.floor(currentPos.x);
				int curY = MathHelper.floor(currentPos.y);
				int curZ = MathHelper.floor(currentPos.z);

				final BlockPos curPos = new BlockPos(curX, curY, curZ);
				T foundRegion = getRelevantRegionAt(player.world, dataType, curPos, endPos, action, shapeLocator);
				final T result = action.onFoundShape(foundRegion, curPos);

				if (result != null)
				{
					return result;
				}

				int i = 200;

				while (i-- >= 0)
				{
					if (Double.isNaN(currentPos.x) || Double.isNaN(currentPos.y) || Double.isNaN(currentPos.z))
					{
						return null;
					}

					if (curX == endX && curY == endY && curZ == endZ)
					{
						return null;
					}

					boolean currentXnotEnd = true;
					boolean currentYnotEnd = true;
					boolean currentZnotEnd = true;

					double newX = 999.0D;
					double newY = 999.0D;
					double newZ = 999.0D;

					if (endX > curX)
					{
						newX = curX + 1.0D;
					}
					else if (endX < curX)
					{
						newX = curX + 0.0D;
					}
					else
					{
						currentXnotEnd = false;
					}

					if (endY > curY)
					{
						newY = curY + 1.0D;
					}
					else if (endY < curY)
					{
						newY = curY + 0.0D;
					}
					else
					{
						currentYnotEnd = false;
					}

					if (endZ > curZ)
					{
						newZ = curZ + 1.0D;
					}
					else if (endZ < curZ)
					{
						newZ = curZ + 0.0D;
					}
					else
					{
						currentZnotEnd = false;
					}

					double distanceFactorX = 999.0D;
					double distanceFactorY = 999.0D;
					double distanceFactorZ = 999.0D;
					final double disX = endPos.x - currentPos.x;
					final double disY = endPos.y - currentPos.y;
					final double disZ = endPos.z - currentPos.z;

					if (currentXnotEnd)
					{
						distanceFactorX = (newX - currentPos.x) / disX;
					}

					if (currentYnotEnd)
					{
						distanceFactorY = (newY - currentPos.y) / disY;
					}

					if (currentZnotEnd)
					{
						distanceFactorZ = (newZ - currentPos.z) / disZ;
					}

					final byte distance;

					if (distanceFactorX < distanceFactorY && distanceFactorX < distanceFactorZ)
					{
						if (endX > curX)
						{
							distance = 4;
						}
						else
						{
							distance = 5;
						}
						currentPos = new Vec3d(newX, currentPos.y + disY * distanceFactorX, currentPos.z + disZ * distanceFactorX);
					}
					else if (distanceFactorY < distanceFactorZ)
					{
						if (endY > curY)
						{
							distance = 0;
						}
						else
						{
							distance = 1;
						}
						currentPos = new Vec3d(currentPos.x + disX * distanceFactorY, newY, currentPos.z + disZ * distanceFactorY);
					}
					else
					{
						if (endZ > curZ)
						{
							distance = 2;
						}
						else
						{
							distance = 3;
						}

						currentPos = new Vec3d(currentPos.x + disX * distanceFactorZ, currentPos.y + disY * distanceFactorZ, newZ);
					}
					curX = MathHelper.floor(currentPos.x);
					curY = MathHelper.floor(currentPos.y);
					curZ = MathHelper.floor(currentPos.z);

					if (distance == 5)
					{
						--curX;
					}

					if (distance == 1)
					{
						--curY;
					}

					if (distance == 3)
					{
						--curZ;
					}

					foundRegion = getRelevantRegionAt(player.world, dataType, new BlockPos(curX, curY, curZ), endPos, action, shapeLocator);
					if (foundRegion != null)
					{
						return foundRegion;
					}
				}
			}
		}
		return null;
	}

	public interface LocateWithPos<T>
	{
		T getIntersectingShape(World world, BlockPos pos);
	}

	public static class RaytraceAction<T>
	{
		public T onFoundShape(final T foundRegion, final BlockPos pos)
		{
			return foundRegion;
		}

		public T onFoundShape(final T foundRegion, final BlockPos pos, final Vec3d endposition)
		{
			return foundRegion;
		}

		public T onIterateOutsideRegion(final BlockPos pos, final Vec3d endposition)
		{
			return null;
		}
	}

}