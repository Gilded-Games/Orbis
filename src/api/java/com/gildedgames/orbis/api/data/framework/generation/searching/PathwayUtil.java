package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class PathwayUtil
{
	public static EnumFacing sideOfConnection(IRegion rect, BlockPos conn)
	{
		if (conn.getX() == rect.getMin().getX())
			return EnumFacing.WEST;
		if (conn.getZ() == rect.getMin().getZ())
			return EnumFacing.SOUTH;
		if (conn.getX() == rect.getMax().getX())
			return EnumFacing.EAST;
		if (conn.getZ() == rect.getMax().getZ())
			return EnumFacing.NORTH;
		if (conn.getY() == rect.getMin().getY())
			return EnumFacing.DOWN;
		if (conn.getY() == rect.getMax().getY())
			return EnumFacing.UP;
		throw new IllegalStateException();
	}

	public static BlockPos adjacent(BlockPos pos, EnumFacing facing)
	{
		return new BlockPos(pos.getX() + facing.getDirectionVec().getX(), pos.getY() + facing.getDirectionVec().getY(), pos.getZ() + facing.getDirectionVec().getZ());
	}

	public static BlockPos rotate(BlockPos pos, Region rect, EnumFacing facing)
	{
		if (facing == EnumFacing.NORTH)
			return pos;
		BlockPos center = rect.getCenter();

		int relX = pos.getX() - center.getX();
		int relZ = pos.getZ() - center.getZ();

		if (facing == EnumFacing.WEST)
		{
			return new BlockPos(center.getX() + relZ, pos.getY(), center.getZ() - relX);
		}
		if (facing == EnumFacing.EAST)
		{
			return new BlockPos(center.getX() - relZ, pos.getY(), center.getZ() + relX);
		}
		return new BlockPos(center.getX() - relX, pos.getY(), center.getZ() - relZ);
	}
}
