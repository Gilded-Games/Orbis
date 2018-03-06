package com.gildedgames.orbis.api.data.pathway;

import com.gildedgames.orbis.api.data.region.IColored;
import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class Entrance implements NBT, IColored
{
	private IMutableRegion bounds;

	private PathwayData toConnectTo;

	private EnumFacing[] facings;

	private Entrance()
	{

	}

	public Entrance(IMutableRegion bounds, PathwayData toConnectTo, EnumFacing[] facings)
	{
		this.bounds = bounds;
		this.toConnectTo = toConnectTo;
		this.facings = facings;
	}

	public IMutableRegion getBounds()
	{
		return this.bounds;
	}

	public PathwayData toConnectTo()
	{
		return this.toConnectTo;
	}

	public EnumFacing[] getFacings()
	{
		return this.facings;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("bounds", this.bounds);
		funnel.set("pathway", this.toConnectTo);
		funnel.setEnumArray("facings", this.facings);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.bounds = funnel.get("bounds");
		this.toConnectTo = funnel.get("pathway");

		String[] names = funnel.getEnumArrayNames("facings");
		this.facings = new EnumFacing[names.length];

		for (int i = 0; i < names.length; i++)
		{
			String name = names[i];
			this.facings[i] = EnumFacing.valueOf(name);
		}
	}

	@Override
	public int getColor()
	{
		return 0xd38dc7;
	}
}
