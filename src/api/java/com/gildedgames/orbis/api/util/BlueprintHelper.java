package com.gildedgames.orbis.api.util;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.region.IShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlueprintHelper
{

	public static BlockDataContainer fetchBlocksInside(final IShape shape, final World world)
	{
		final BlockDataContainer container = new BlockDataContainer(shape.getBoundingBox());

		final BlockPos min = shape.getBoundingBox().getMin();

		for (final BlockPos pos : shape.createShapeData())
		{
			final BlockData blockData = new BlockData().getDataFrom(pos, world);

			final BlockPos translated = pos.add(-min.getX(), -min.getY(), -min.getZ());

			container.set(blockData, translated);
		}

		return container;
	}

}
