package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class WorldActionBase implements IWorldAction
{
	private long seed;

	private boolean seedSet;

	public long getSeed()
	{
		return this.seed;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (!this.seedSet)
		{
			this.seed = world.rand.nextLong();
			this.seedSet = true;
		}
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{

	}

	@Override
	public void write(NBTTagCompound tag)
	{
		tag.setLong("seed", this.seed);
		tag.setBoolean("seedSet", this.seedSet);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		this.seed = tag.getLong("seed");
		this.seedSet = tag.getBoolean("seedSet");
	}
}
