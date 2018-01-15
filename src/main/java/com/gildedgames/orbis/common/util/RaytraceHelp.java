package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class RaytraceHelp
{

	public static BlockPos fromMovingObjectPos(final RayTraceResult position)
	{
		final Vec3d pos = position.hitVec;
		return new BlockPos(pos.x, pos.y, pos.z);
	}

	public static BlockPos rayTrace(final PlayerOrbis player)
	{
		return rayTrace(player.getReach(), 1.0F, player.getEntity());
	}

	public static BlockPos rayTrace(final double blockReachDistance, final float partialTicks, final Entity entity)
	{
		return entity.rayTrace(blockReachDistance, partialTicks).getBlockPos();
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

	public static RayTraceResult rayTraceNoBlocks(final double blockReachDistance, final float partialTicks, final Entity entity)
	{
		final Vec3d vec3 = getPositionEyes(partialTicks, entity);
		final Vec3d vec31 = entity.getLook(partialTicks);
		final Vec3d vec32 = vec3.addVector(vec31.x * blockReachDistance, vec31.y * blockReachDistance, vec31.z * blockReachDistance);

		return new RayTraceResult(entity, vec32);
	}

	public static BlockPos doOrbisRaytrace(final PlayerOrbis player)
	{
		final RayTraceResult raytrace = rayTraceNoBlocks(player.getReach(), 1.0F, player.getEntity());

		if (raytrace != null)
		{
			return fromMovingObjectPos(raytrace);
		}

		return player.raytraceWithRegionSnapping();
	}

	public static BlockPos doOrbisRaytrace(final PlayerOrbis player, final BlockPos airRayTrace)
	{
		final RayTraceResult raytrace = rayTraceNoBlocks(player.getReach(), 1.0F, player.getEntity());

		if (raytrace != null)
		{
			return fromMovingObjectPos(raytrace);
		}

		return airRayTrace;
	}

}
