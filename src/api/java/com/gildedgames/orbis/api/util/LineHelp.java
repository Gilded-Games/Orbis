package com.gildedgames.orbis.api.util;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class LineHelp
{

	public static Iterable<BlockPos.MutableBlockPos> createLinePositions(final int lineRadius, final BlockPos start, final BlockPos end)
	{
		final List<BlockPos.MutableBlockPos> lineData = new ArrayList<>();

		for (int lineX = -lineRadius + 1; lineX < lineRadius - (lineRadius == 1 ? 0 : 1); lineX++)
		{
			for (int lineY = -lineRadius + 1; lineY < lineRadius - (lineRadius == 1 ? 0 : 1); lineY++)
			{
				for (int lineZ = -lineRadius + 1; lineZ < lineRadius - (lineRadius == 1 ? 0 : 1); lineZ++)
				{
					BlockPos s = start.add(lineX, lineY, lineZ);
					BlockPos e = end.add(lineX, lineY, lineZ);

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

					int lineSegments = 0;

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
									lineData.add(new BlockPos.MutableBlockPos(xCopy + x1, yCopy + y1, zCopy + z1));
								}
							}
						}

						lineSegments++;

						if (lineSegments % 5 == 0)
						{
							//size = Math.max(1, size - 1);
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
		}

		return lineData;
	}
}
