package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.lib.block.BlockData;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldActionBlockPlace implements IWorldAction
{

	private BlockData before, after;

	private BlockPos pos;

	private CreationDataOrbis creationData;

	private WorldActionBlockPlace()
	{

	}

	public WorldActionBlockPlace(BlockData before, BlockData after, BlockPos pos)
	{
		this.before = before;
		this.after = after;
		this.pos = pos;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (this.creationData == null)
		{
			this.creationData = new CreationDataOrbis(world);
			this.creationData.schedules(false).pos(this.pos);
		}
		else
		{
			this.creationData.world(world);
			this.creationData.creator(player.getEntity());
		}

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));
		primer.setBlockInWorld(this.after, this.creationData);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (this.creationData == null)
		{
			this.creationData = new CreationDataOrbis(world);
			this.creationData.schedules(false).pos(this.pos);
		}

		this.creationData.world(world);
		this.creationData.creator(player.getEntity());
		this.creationData.placesAir(true);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.setBlockInWorld(this.before, this.creationData);
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{

	}

	@Override
	public boolean isTemporary()
	{
		return false;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("b", this.before);
		funnel.set("a", this.after);
		funnel.setPos("p", this.pos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.before = funnel.get("b");
		this.after = funnel.get("a");
		this.pos = funnel.getPos("p");
	}
}
