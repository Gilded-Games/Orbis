package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.data.pathway.Entrance;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionAddEntrance implements IWorldAction
{

	private Blueprint blueprint;

	private Entrance entrance;

	private WorldActionAddEntrance()
	{

	}

	public WorldActionAddEntrance(Blueprint blueprint, Entrance entrance)
	{
		this.blueprint = blueprint;
		this.entrance = entrance;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		this.blueprint.getData().addEntrance(this.entrance);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		this.blueprint.getData().removeEntrance(this.entrance);
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{
		this.blueprint.setWorld(world);
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("e", this.entrance);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.entrance = funnel.get("e");
	}
}
