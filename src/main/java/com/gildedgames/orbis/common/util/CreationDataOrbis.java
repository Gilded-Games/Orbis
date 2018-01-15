package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreationDataOrbis extends CreationData
{
	public CreationDataOrbis(final World world)
	{
		super(world);
	}

	public CreationDataOrbis(final World world, final long seed)
	{
		super(world, seed);
	}

	public CreationDataOrbis(final World world, final EntityPlayer creator)
	{
		super(world, creator);
	}

	@Override
	public boolean shouldCreate(final BlockData data, final BlockPos pos)
	{
		if (this.schedules())
		{
			final WorldObjectManager manager = WorldObjectManager.get(this.getWorld());
			final IWorldObjectGroup group = manager.getGroup(0);

			return group.getIntersectingShape(pos) != null;
		}

		return true;
	}
}
