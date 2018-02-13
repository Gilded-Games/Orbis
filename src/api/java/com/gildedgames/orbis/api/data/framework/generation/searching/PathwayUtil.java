package com.gildedgames.orbis.api.data.framework.generation.searching;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RotationHelp;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class PathwayUtil
{
	public static EnumFacing[] getRotated(EnumFacing[] faces, Rotation rotation)
	{
		int rotAmount = RotationHelp.getRotationAmount(rotation, Rotation.NONE);
		boolean goClockwise = RotationHelp.getGoClockwise(rotation, Rotation.NONE);

		EnumFacing[] newFaces = new EnumFacing[faces.length];

		for (int i = 0; i < faces.length; i++)
		{
			EnumFacing newFace = faces[i];

			if (newFace == EnumFacing.DOWN || newFace == EnumFacing.UP)
			{
				newFaces[i] = newFace;
				continue;
			}

			for (int j = 0; j < rotAmount; j++)
			{
				newFace = goClockwise ? newFace.rotateY() : newFace.rotateYCCW();
			}

			newFaces[i] = newFace;
		}

		return newFaces;
	}

	public static EnumFacing[] getOpposites(EnumFacing[] faces)
	{
		EnumFacing[] opposites = new EnumFacing[faces.length];

		for (int i = 0; i < faces.length; i++)
		{
			opposites[i] = faces[i].getOpposite();
		}

		return opposites;
	}

	//TODO: Needs to return multiple facings for corners. Need to change referenced code to accommodate this
	public static EnumFacing[] sidesOfConnection(IRegion rect, IRegion conn)
	{
		boolean WEST = false, NORTH = false, EAST = false, SOUTH = false, DOWN = false, UP = false;

		int size = 0;

		if (conn.getMin().getX() == rect.getMin().getX() && (conn.getLength() > 1 || rect.getHeight() <= 1 || conn.getHeight() > 1))
		{
			WEST = true;
			size++;
		}
		if (conn.getMin().getZ() == rect.getMin().getZ() && (conn.getWidth() > 1 || rect.getHeight() <= 1 || conn.getHeight() > 1))
		{
			NORTH = true;
			size++;
		}
		if (conn.getMin().getX() == rect.getMax().getX() && (conn.getLength() > 1 || rect.getHeight() <= 1 || conn.getHeight() > 1))
		{
			EAST = true;
			size++;
		}
		if (conn.getMin().getZ() == rect.getMax().getZ() && (conn.getWidth() > 1 || rect.getHeight() <= 1 || conn.getHeight() > 1))
		{
			SOUTH = true;
			size++;
		}
		if (conn.getMin().getY() == rect.getMin().getY() && conn.getHeight() == 1)
		{
			DOWN = true;
			size++;
		}
		if (conn.getMin().getY() == rect.getMax().getY() && conn.getHeight() == 1)
		{
			UP = true;
			size++;
		}

		EnumFacing[] faces = new EnumFacing[size];

		int index = 0;

		if (WEST)
		{
			faces[index] = EnumFacing.WEST;
			index++;
		}
		if (NORTH)
		{
			faces[index] = EnumFacing.NORTH;
			index++;
		}
		if (EAST)
		{
			faces[index] = EnumFacing.EAST;
			index++;
		}
		if (SOUTH)
		{
			faces[index] = EnumFacing.SOUTH;
			index++;
		}
		if (DOWN)
		{
			faces[index] = EnumFacing.DOWN;
			index++;
		}
		if (UP)
		{
			faces[index] = EnumFacing.UP;
		}

		return faces;
	}

	public static BlockPos adjacent(BlockPos pos, EnumFacing facing)
	{
		return new BlockPos(pos.getX() + facing.getDirectionVec().getX(), pos.getY() + facing.getDirectionVec().getY(),
				pos.getZ() + facing.getDirectionVec().getZ());
	}

	public static IRegion adjacent(IRegion region, EnumFacing facing)
	{
		return new Region(adjacent(region.getMin(), facing), adjacent(region.getMax(), facing));
	}

}