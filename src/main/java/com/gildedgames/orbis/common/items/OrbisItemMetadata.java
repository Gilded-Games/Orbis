package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis_api.data.region.IDimensions;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.util.mc.NBT;
import net.minecraft.nbt.NBTTagCompound;

public class OrbisItemMetadata implements NBT
{
	private String name;

	private IDimensions dimensions;

	private OrbisItemMetadata()
	{

	}

	public OrbisItemMetadata(String name, IDimensions dimensions)
	{
		this.name = name;
		this.dimensions = dimensions;
	}

	public String getName()
	{
		return this.name;
	}

	public IDimensions getDimensions()
	{
		return this.dimensions;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("name", this.name);
		funnel.set("dimensions", this.dimensions);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.name = tag.getString("name");
		this.dimensions = funnel.get("dimensions");
	}
}
