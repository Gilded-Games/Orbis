package com.gildedgames.orbis.common.data;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.pathway.PathwayData;
import com.gildedgames.orbis_api.data.region.IMutableRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BlueprintNode implements IFrameworkNode
{

	private BlueprintData data;

	private BlueprintDataPalette palette;

	private List<BlueprintData> values;

	private int maxEntrances;

	private IMutableRegion bounds;

	private FrameworkData dataParent;

	private BlueprintNode()
	{

	}

	public BlueprintNode(BlueprintData data)
	{
		this.data = data;
		this.values = Lists.newArrayList(data);

		this.maxEntrances = this.data.entrances().size();
	}

	public BlueprintNode(BlueprintDataPalette palette)
	{
		this.palette = palette;
		this.values = Lists.newArrayList(this.palette.getData());

		this.maxEntrances = this.palette.getMaximumEntrances();
	}

	@Override
	public IMutableRegion getBounds()
	{
		if (this.bounds == null)
		{
			this.bounds = new Region(this.data != null ? this.data : this.palette.getLargestDim());
		}

		return this.bounds;
	}

	@Override
	public BlueprintData getBlueprintData()
	{
		return null;
	}

	@Override
	public int getMaxEdges()
	{
		return this.maxEntrances;
	}

	@Override
	public Collection<PathwayData> pathways()
	{
		return Collections.emptyList();
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("data", this.data.getMetadata().getIdentifier());
		funnel.set("palette", this.palette);
		funnel.set("bounds", this.bounds);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);
		IDataIdentifier id = funnel.get("data");

		Optional<IData> data = OrbisAPI.services().getProjectManager().findData(id);

		if (data.isPresent())
		{
			this.data = (BlueprintData) data.get();
		}
		else
		{
			OrbisCore.LOGGER.error("Could not load back BlueprintData", id, this.getClass());
		}

		this.palette = funnel.get("palette");
		this.bounds = funnel.get("bounds");

		if (this.data != null)
		{
			this.maxEntrances = this.data.entrances().size();
			this.values = Lists.newArrayList(this.data);
		}
		else
		{
			this.maxEntrances = this.palette.getMaximumEntrances();
			this.values = Lists.newArrayList(this.palette.getData());
		}
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		if (this.data != null)
		{
			builder.append(this.data);
		}
		else
		{
			builder.append(this.palette);
		}

		return builder.toHashCode();
	}

	@Override
	public Class<? extends FrameworkData> getDataClass()
	{
		return FrameworkData.class;
	}

	@Override
	public FrameworkData getDataParent()
	{
		return this.dataParent;
	}

	@Override
	public void setDataParent(FrameworkData frameworkData)
	{
		this.dataParent = frameworkData;
	}
}
