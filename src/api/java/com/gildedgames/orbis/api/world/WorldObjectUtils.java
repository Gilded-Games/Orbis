package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.google.common.collect.Lists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class WorldObjectUtils
{
	public static IShape getIntersectingShape(World world, final BlockPos pos)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (area.contains(pos))
				{
					return area;
				}
			}
		}

		return null;
	}

	public static <T extends IShape> T getIntersectingShape(World world, final Class<T> shapeType, final BlockPos pos)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (area.contains(pos) && area.getClass() == shapeType)
				{
					return shapeType.cast(area);
				}
			}
		}

		return null;
	}

	public static IShape getIntersectingShape(World world, final IShape shape)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.intersects2D(area, shape))
				{
					return area;
				}
			}

		}

		return null;
	}

	public static <T extends IShape> T getIntersectingShape(World world, final Class<T> shapeType, final IShape shape)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.intersects2D(area, shape) && area.getClass() == shapeType)
				{
					return shapeType.cast(area);
				}
			}
		}

		return null;
	}

	public static <T extends IShape> List<T> getIntersectingShapes(World world, final Class<T> shapeType, final IShape shape)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		final List<T> intersecting = Lists.newArrayList();

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.intersects2D(area, shape) && area.getClass() == shapeType)
				{
					intersecting.add(shapeType.cast(area));
				}
			}
		}

		return intersecting;
	}

	public static IShape getContainedShape(World world, final IShape shape)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.contains(area, shape))
				{
					return area;
				}
			}
		}

		return null;
	}

	public static <T extends IShape> T getContainedShape(World world, final Class<T> shapeType, final IShape shape)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.contains(area, shape) && area.getClass() == shapeType)
				{
					return shapeType.cast(area);
				}
			}
		}

		return null;
	}

	public static <T extends IShape> List<T> getContainedShapes(World world, final Class<T> shapeType, final IShape shape)
	{
		WorldObjectManager manager = WorldObjectManager.get(world);

		final List<T> contained = Lists.newArrayList();

		for (final IWorldObject obj : manager.getObjects())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.contains(shape, area))
				{
					if (area.getClass() == shapeType)
					{
						contained.add(shapeType.cast(area));
					}
				}
			}
		}

		return contained;
	}

	public static boolean isIntersectingShapes(World world, final IShape shape)
	{
		return getIntersectingShape(world, shape) != null;
	}

	public static boolean isContainedInShape(World world, final IShape shape)
	{
		return getContainedShape(world, shape) != null;
	}

}
