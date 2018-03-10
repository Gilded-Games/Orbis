package com.gildedgames.orbis.api.data.framework;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.pathway.PathwayData;
import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class FrameworkNode implements IFrameworkNode
{
	// Not sure what this is about tbh lol.
	private static boolean isNullAllowed = false;

	private IFrameworkNode schedule;

	//	private final boolean isNullAllowed = false;

	private IWorldObject worldObjectParent;

	private FrameworkNode()
	{

	}

	public FrameworkNode(IFrameworkNode schedule)
	{
		this.schedule = schedule;
	}

	public IFrameworkNode schedule()
	{
		return this.schedule;
	}

	@Override
	public int maxEdges()
	{
		return this.schedule.maxEdges();
	}

	@Override
	public IMutableRegion getBounds()
	{
		return this.schedule.getBounds();
	}

	@Override
	public List<BlueprintData> possibleValues(Random random)
	{
		final List<BlueprintData> superPossibleValues = this.schedule.possibleValues(random);
		if (isNullAllowed && !superPossibleValues.contains(null))
		{
			superPossibleValues.add(null);
		}
		return superPossibleValues;
	}

	@Override
	public Collection<PathwayData> pathways()
	{
		return this.schedule.pathways();
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);
		funnel.set("schedule", this.schedule);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);
		this.schedule = funnel.get("schedule");
	}

	@Override
	public IWorldObject getWorldObjectParent()
	{
		return this.worldObjectParent;
	}

	@Override
	public void setWorldObjectParent(IWorldObject parent)
	{
		this.worldObjectParent = parent;

		if (this.schedule != null)
		{
			this.schedule.setWorldObjectParent(this.worldObjectParent);
		}
	}
}
