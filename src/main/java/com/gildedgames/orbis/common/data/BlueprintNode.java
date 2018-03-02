package com.gildedgames.orbis.common.data;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlueprintNode implements IFrameworkNode
{

	private BlueprintData data;

	private BlueprintDataPalette palette;

	private List<BlueprintData> values;

	private int maxEntrances;

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
	public IDimensions largestPossibleDim()
	{
		return this.data != null ? this.data : this.palette.getLargestInArea();
	}

	@Override
	public List<BlueprintData> possibleValues(Random random)
	{
		return this.values;
	}

	@Override
	public int maxEdges()
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

		funnel.set("data", this.data);
		funnel.set("palette", this.palette);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.data = funnel.get("data");
		this.palette = funnel.get("palette");

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
}
