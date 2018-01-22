package com.gildedgames.orbis.api.data.pathway;

import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class Entrance implements NBT
{
	private BlockPos pos;

	private PathwayData toConnectTo;

	public Entrance(BlockPos pos, PathwayData toConnectTo)
	{
		this.pos = pos;
		this.toConnectTo = toConnectTo;
	}

	public BlockPos getPos()
	{
		return this.pos;
	}

	public PathwayData toConnectTo()
	{
		return this.toConnectTo;
	}

	public void setPos(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);
		funnel.set("pathway", this.toConnectTo);
		funnel.setBlockPos("pos", this.pos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);
		this.toConnectTo = funnel.get("pathway");
		this.pos = funnel.getBlockPos("pos");
	}
}
