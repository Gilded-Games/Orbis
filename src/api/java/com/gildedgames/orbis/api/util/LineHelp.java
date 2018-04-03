package com.gildedgames.orbis.api.util;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class LineHelp
{

	public static Iterable<BlockPos.MutableBlockPos> createLinePositions(final int lineRadius, final BlockPos start, final BlockPos end)
	{
		final List<BlockPos.MutableBlockPos> lineData = new ArrayList<>();

		//final double angle = Math.atan2(start.getX() - end.getX(), start.getZ() - start.getZ());

		for (int i = 0; i < lineRadius; i++)
		{
			for (int j = 0; j < (i != 0 ? 2 : 1); j++)
			{
				BlockPos s = start;
				BlockPos e = end;

				/*if (i != 0)
				{
					final double xDif = i / (2 * Math.tan(angle));

					double nextX = s.getX() + (j == 0 ? i : -i);
					double nextZ = s.getZ() + 1;

					final double xr = s.getX() + (nextX - s.getX()) * Math.cos(angle) - (nextZ - s.getZ()) * Math.sin(angle);
					final double zr = s.getZ() + (nextX - s.getX()) * Math.sin(angle) - (nextZ - s.getZ()) * Math.cos(angle);

					nextX = e.getX() + (j == 0 ? i : -i);
					nextZ = e.getZ() + 1;

					final double xre = e.getX() + (nextX - e.getX()) * Math.cos(angle) - (nextZ - e.getZ()) * Math.sin(angle);
					final double zre = e.getZ() + (nextX - e.getX()) * Math.sin(angle) - (nextZ - e.getZ()) * Math.cos(angle);

					s = new BlockPos(xr, s.getY(), zr);
					e = new BlockPos(xre, e.getY(), zre);
				}*/

				final boolean steepXY = Math.abs(e.getY() - s.getY()) > Math.abs(e.getX() - s.getX());

				if (steepXY)
				{
					int tempY = s.getY();

					s = new BlockPos(tempY, s.getX(), s.getZ());

					tempY = e.getY();

					e = new BlockPos(tempY, e.getX(), e.getZ());
				}

				final boolean steepXZ = Math.abs(e.getZ() - s.getZ()) > Math.abs(e.getX() - s.getX());

				if (steepXZ)
				{
					int tempZ = s.getZ();

					s = new BlockPos(tempZ, s.getY(), s.getX());

					tempZ = e.getZ();

					e = new BlockPos(tempZ, e.getY(), e.getX());
				}

				final int deltaX = Math.abs(e.getX() - s.getX());
				final int deltaY = Math.abs(e.getY() - s.getY());
				final int deltaZ = Math.abs(e.getZ() - s.getZ());

				int errorXY = deltaX / 2;
				int errorXZ = deltaX / 2;

				final int stepX = s.getX() > e.getX() ? -1 : 1;
				final int stepY = s.getY() > e.getY() ? -1 : 1;
				final int stepZ = s.getZ() > e.getZ() ? -1 : 1;

				int z = s.getZ();
				int y = s.getY();

				for (int x = s.getX(); x != e.getX(); x += stepX)
				{
					int xCopy = x, yCopy = y, zCopy = z;

					if (steepXZ)
					{
						final int tempZ = zCopy;

						zCopy = xCopy;
						xCopy = tempZ;
					}

					if (steepXY)
					{
						final int tempY = yCopy;

						yCopy = xCopy;
						xCopy = tempY;
					}

					for (int x1 = 0; x1 != stepX; x1 += stepX)
					{
						for (int z1 = 0; z1 != stepZ; z1 += stepZ)
						{
							for (int y1 = 0; y1 != stepY; y1 += stepY)
							{
								final BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(xCopy + x1, yCopy + y1, zCopy + z1);

								if (!lineData.contains(p))
								{
									lineData.add(p);
								}
							}
						}
					}

					errorXY -= deltaY;
					errorXZ -= deltaZ;

					if (errorXY < 0)
					{
						y += stepY;
						errorXY += deltaX;
					}

					if (errorXZ < 0)
					{
						z += stepZ;
						errorXZ += deltaX;
					}
				}
			}
		}

		return lineData;
	}
}
