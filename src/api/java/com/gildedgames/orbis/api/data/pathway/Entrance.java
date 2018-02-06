package com.gildedgames.orbis.api.data.pathway;

import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.nbt.NBTTagCompound;

public class Entrance implements NBT
{
	private IMutableRegion bounds;

	private PathwayData toConnectTo;

	public Entrance(IMutableRegion bounds, PathwayData toConnectTo)
	{
		this.bounds = bounds;
		this.toConnectTo = toConnectTo;
	}

	public IMutableRegion getBounds()
	{
		return this.bounds;
	}

	public PathwayData toConnectTo()
	{
		return this.toConnectTo;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("bounds", this.bounds);
		funnel.set("pathway", this.toConnectTo);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.bounds = funnel.get("bounds");
		this.toConnectTo = funnel.get("pathway");
	}
}
