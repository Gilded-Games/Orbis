package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis_api.block.BlockData;
import com.gildedgames.orbis_api.core.CreationData;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreationDataOrbis extends CreationData
{
	private CreationDataOrbis()
	{
		super();
	}

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
			return WorldObjectUtils.getIntersectingShape(this.getWorld(), pos) != null;
		}

		return true;
	}
}
