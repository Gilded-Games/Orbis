package com.gildedgames.orbis.api.data.blueprint;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.IDataHolder;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Random;

public class BlueprintDataHolder implements IDataHolder<BlueprintData>
{
	private BlueprintData data;

	private BlueprintDataHolder()
	{

	}

	public BlueprintDataHolder(BlueprintData data)
	{
		this.data = data;
	}

	@Override
	public BlueprintData get(World world, Random random)
	{
		return this.data;
	}

	@Override
	public int getLargestHeight()
	{
		return this.data.getHeight();
	}

	@Override
	public int getLargestWidth()
	{
		return this.data.getWidth();
	}

	@Override
	public int getLargestLength()
	{
		return this.data.getLength();
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.data.getMetadata().getIdentifier());
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		try
		{
			final IDataIdentifier id = funnel.get("id");
			this.data = OrbisAPI.services().getProjectManager().findData(id);
		}
		catch (final OrbisMissingDataException | OrbisMissingProjectException e)
		{
			OrbisAPI.LOGGER.error("Missing in " + this.getClass().getName() + " : ", e);
		}
	}
}
