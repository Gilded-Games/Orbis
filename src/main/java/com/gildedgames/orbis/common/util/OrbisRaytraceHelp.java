package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis.client.OrbisKeyBindings;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.pathway.IEntrance;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrbisRaytraceHelp
{

	public static final LocateWithPos<IShape> WORLD_OBJECT_LOCATOR = (world, pos, prevPos) -> WorldObjectUtils.getIntersectingShape(world, pos);

	public static final LocateWithPos<IFrameworkNode> FRAMEWORK_NODE_LOCATOR = (world, pos, prevPos) ->
			WorldObjectUtils.getIntersectingShape(world, pos)
					.filter(Framework.class::isInstance)
					.map(Framework.class::cast)
					.flatMap(f -> f.findIntersectingNode(pos));

	public static final LocateWithPos<ISchedule> SCHEDULE_LOCATOR = (world, pos, prevPos) ->
			WorldObjectUtils.getIntersectingShape(world, pos)
					.filter(Blueprint.class::isInstance)
					.map(Blueprint.class::cast)
					.flatMap(b -> b.findIntersectingSchedule(pos));

	public static final LocateWithPos<IEntrance> ENTRANCE_LOCATOR = (world, pos, prevPos) ->
			WorldObjectUtils.getIntersectingShape(world, pos)
					.filter(Blueprint.class::isInstance)
					.map(Blueprint.class::cast)
					.flatMap(b -> RegionHelp.findIntersecting(b.getData().entrances(), b.getPos(), pos));

	private static final List<Class<? extends IWorldObject>> blueprintClass;

	static
	{
		blueprintClass = new ArrayList<>(1);
		blueprintClass.add(Blueprint.class);
	}

	private static <T> T locate(World world, final BlockPos pos,
			final Vec3d endPosition, BlockPos prevPos, final RaytraceAction<T> action, LocateWithPos<T> shapeLocator)
	{
		return shapeLocator.locate(world, pos, prevPos).map(foundRegion -> action.onLocate(foundRegion, pos, endPosition))
				.orElse(action.onNotLocate(pos, endPosition));
	}

	public static double getFinalExtendedReach(final EntityPlayer player, double currentReach)
	{
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

		return currentReach;
	}

	/**
	 * Returns the BlockPos the player is selecting with
	 * extended reach. This doesn't take into account
	 * things like snapping and corners.
	 * @return
	 */
	public static BlockPos raytraceNoSnapping(final EntityPlayer player)
	{
		final RayTraceResult finalVec = getStandardRaytrace(player);

		if (finalVec == null)
		{
			return BlockPos.ORIGIN;
		}

		final int x = MathHelper.floor(finalVec.hitVec.x);
		final int y = MathHelper.floor(finalVec.hitVec.y);
		final int z = MathHelper.floor(finalVec.hitVec.z);

		return new BlockPos(x, y, z);
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

	public static <T> T raytraceLocateObject(final EntityPlayer player, final float partialTicks, final LocateWithPos<T> shapeLocator)
	{
		final RayTraceResult focus = getStandardRaytrace(player);

		if (focus == null)
		{
			return null;
		}

		return raytraceLocateObject(player, getPositionEyes(partialTicks, player), focus.hitVec, shapeLocator);
	}

	public static RayTraceResult getStandardRaytrace(final EntityPlayer player)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (playerOrbis == null)
		{
			return null;
		}

		final double reach = playerOrbis.getDeveloperReach();
		Vec3d lookVec = player.getLookVec();

		final Vec3d startPos = getPositionEyes(1.0F, player);
		final Vec3d endPos = startPos.addVector(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

		RayTraceResult blockRaytrace = raytraceLocateObject(player, startPos, endPos,
				(world, pos, prevPos) ->
				{
					Optional<Blueprint> shape = WorldObjectUtils.getIntersectingShape(world, pos, Blueprint.class);
					Optional<Blueprint> prevShape = WorldObjectUtils.getIntersectingShape(world, prevPos, Blueprint.class);

					Optional<RayTraceResult> result = shape.flatMap(blueprint -> {
						if (blueprint.contains(pos) && blueprint.getCurrentScheduleLayerNode() != null)
						{
							IBlockState state = blueprint.getCurrentScheduleLayerNode().getData().getStateRecord()
									.get(pos.getX() - blueprint.getMin().getX(), pos.getY() - blueprint.getMin().getY(),
											pos.getZ() - blueprint.getMin().getZ());
							return state == null ? Optional.empty() : Optional.of(new RayTraceResult(player, new Vec3d(pos)));
						}
						return Optional.empty();
					});
					if (result.isPresent())
					{
						return result;
					}

					result = prevShape.flatMap(blueprint ->
							shape.map(current -> current != blueprint).orElse(true) ?
									Optional.of(new RayTraceResult(player, new Vec3d(prevPos))) :
									Optional.empty()
					);
					if (result.isPresent())
					{
						return result;
					}

					return world.getBlockState(pos) != Blocks.AIR.getDefaultState() ?
							Optional.of(new RayTraceResult(player, new Vec3d(pos))) :
							Optional.empty();
				});

		RayTraceResult result = OrbisKeyBindings.keyBindControl.isKeyDown() ?
				new RayTraceResult(player, endPos) : blockRaytrace != null ? blockRaytrace : new RayTraceResult(player, endPos);

		Vec3d clampedVec = new Vec3d(MathHelper.floor(result.hitVec.x),
				Math.max(player.world.provider.getDimensionType() == WorldProviderOrbis.ORBIS ? 1 : 0, MathHelper.floor(result.hitVec.y)),
				MathHelper.floor(result.hitVec.z));
		result = new RayTraceResult(player, clampedVec);

		return result;
	}

	public static <T> T raytraceLocateObject(final EntityPlayer player, final Vec3d startPos, final Vec3d endPos, final LocateWithPos<T> shapeLocator)
	{
		return raytraceLocateObject(player, startPos, endPos, new RaytraceAction<>(), shapeLocator);
	}

	private static <T> T raytraceLocateObject(
			final EntityPlayer player, Vec3d startPos, final Vec3d endPos, final RaytraceAction<T> action,
			final LocateWithPos<T> shapeLocator)
	{
		if (player == null)
		{
			return null;
		}

		if (!Double.isNaN(startPos.x) && !Double.isNaN(startPos.y) && !Double.isNaN(startPos.z))
		{
			if (!Double.isNaN(endPos.x) && !Double.isNaN(endPos.y) && !Double.isNaN(endPos.z))
			{
				final int endX = MathHelper.floor(endPos.x);
				final int endY = MathHelper.floor(endPos.y);
				final int endZ = MathHelper.floor(endPos.z);
				int curX = MathHelper.floor(startPos.x);
				int curY = MathHelper.floor(startPos.y);
				int curZ = MathHelper.floor(startPos.z);

				final BlockPos curPos = new BlockPos(curX, curY, curZ);
				T locatedObject = locate(player.world, curPos, endPos, curPos, action, shapeLocator);
				final T result = action.onLocate(locatedObject, curPos);

				if (result != null)
				{
					return result;
				}

				int i = 200;

				while (i-- >= 0)
				{
					if (Double.isNaN(startPos.x) || Double.isNaN(startPos.y) || Double.isNaN(startPos.z))
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
					final double disX = endPos.x - startPos.x;
					final double disY = endPos.y - startPos.y;
					final double disZ = endPos.z - startPos.z;

					if (currentXnotEnd)
					{
						distanceFactorX = (newX - startPos.x) / disX;
					}

					if (currentYnotEnd)
					{
						distanceFactorY = (newY - startPos.y) / disY;
					}

					if (currentZnotEnd)
					{
						distanceFactorZ = (newZ - startPos.z) / disZ;
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
						startPos = new Vec3d(newX, startPos.y + disY * distanceFactorX, startPos.z + disZ * distanceFactorX);
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
						startPos = new Vec3d(startPos.x + disX * distanceFactorY, newY, startPos.z + disZ * distanceFactorY);
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

						startPos = new Vec3d(startPos.x + disX * distanceFactorZ, startPos.y + disY * distanceFactorZ, newZ);
					}

					BlockPos prevPos = new BlockPos(curX, curY, curZ);

					curX = MathHelper.floor(startPos.x);
					curY = MathHelper.floor(startPos.y);
					curZ = MathHelper.floor(startPos.z);

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

					locatedObject = locate(player.world, new BlockPos(curX, curY, curZ), endPos, prevPos, action, shapeLocator);

					if (locatedObject != null)
					{
						return locatedObject;
					}
				}
			}
		}
		return null;
	}

	public interface LocateWithPos<T>
	{
		Optional<T> locate(World world, BlockPos pos, BlockPos prevPos);
	}

	public static class RaytraceAction<T>
	{
		public T onLocate(final T foundRegion, final BlockPos pos)
		{
			return foundRegion;
		}

		public T onLocate(final T foundRegion, final BlockPos pos, final Vec3d endposition)
		{
			return foundRegion;
		}

		public T onNotLocate(final BlockPos pos, final Vec3d endposition)
		{
			return null;
		}
	}

}