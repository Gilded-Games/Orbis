package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis_api.block.BlockInstance;
import com.gildedgames.orbis_api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis_api.processing.DataPrimer;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionBlockDestroy implements IWorldAction
{

	private BlockInstance instance;

	private CreationDataOrbis creationData;

	private WorldActionBlockDestroy()
	{

	}

	public WorldActionBlockDestroy(BlockInstance instance)
	{
		this.instance = instance;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		world.setBlockToAir(this.instance.getPos());
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (this.creationData == null)
		{
			this.creationData = new CreationDataOrbis(world);
			this.creationData.schedules(false);
		}
		else
		{
			this.creationData.world(world);
			this.creationData.creator(player.getEntity());
		}

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.instance, this.creationData);
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{

	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("i", this.instance);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.instance = funnel.get("i");
	}
}
