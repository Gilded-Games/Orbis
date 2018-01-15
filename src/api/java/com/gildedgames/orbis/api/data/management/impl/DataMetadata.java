package com.gildedgames.orbis.api.data.management.impl;

import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.data.management.IDataMetadata;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.IText;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

/**
 * A basic concrete implementation of IDataMetadata.
 * Can be decorated with ProjectMetadataDecorator.
 */
public class DataMetadata implements IDataMetadata
{
	private String name = "";

	private List<IText> display;

	private IDataIdentifier identifier;

	private List<IDataIdentifier> dependencies;

	public DataMetadata()
	{
		this.display = Lists.newArrayList();
		this.dependencies = Lists.newArrayList();
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("name", this.name);
		funnel.set("identifier", this.identifier);

		funnel.setList("dependencies", this.dependencies);
		funnel.setList("display", this.display);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.name = tag.getString("name");
		this.identifier = funnel.get("identifier");

		this.dependencies = funnel.getList("dependencies");
		this.display = funnel.getList("display");
	}

	@Override
	public List<IText> getMetadataDisplay()
	{
		return this.display;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public List<IDataIdentifier> getDependencies()
	{
		return this.dependencies;
	}

	@Override
	public IDataIdentifier getIdentifier()
	{
		return this.identifier;
	}

	@Override
	public void setIdentifier(final IDataIdentifier identifier)
	{
		this.identifier = identifier;
	}
}
