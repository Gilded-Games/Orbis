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

public class WorldActionBlockPlace implements IWorldAction
{

	private BlockInstance before, after;

	private CreationDataOrbis creationData;

	private WorldActionBlockPlace()
	{

	}

	public WorldActionBlockPlace(BlockInstance before, BlockInstance after)
	{
		this.before = before;
		this.after = after;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
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

		primer.create(this.after, this.creationData);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (this.creationData == null)
		{
			this.creationData = new CreationDataOrbis(world);
			this.creationData.schedules(false);
		}

		this.creationData.world(world);
		this.creationData.creator(player.getEntity());
		this.creationData.placesAir(true);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.before, this.creationData);
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{

	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("b", this.before);
		funnel.set("a", this.after);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.before = funnel.get("b");
		this.after = funnel.get("a");
	}
}
