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

public class WorldActionBlockDestroy implements IWorldAction
{

	private BlockData data;

	private BlockPos pos;

	private CreationDataOrbis creationData;

	private WorldActionBlockDestroy()
	{

	}

	public WorldActionBlockDestroy(BlockData data, BlockPos pos)
	{
		this.data = data;
		this.pos = pos;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		world.setBlockToAir(this.pos);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
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

		primer.setBlockInWorld(this.data.getBlockState(), this.data.getTileEntity(), this.creationData.getPos(), this.creationData);
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

		funnel.set("i", this.data);
		funnel.setPos("p", this.pos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.data = funnel.get("i");
		this.pos = funnel.getPos("p");
	}
}
