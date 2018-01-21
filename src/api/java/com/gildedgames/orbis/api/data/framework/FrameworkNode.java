package com.gildedgames.orbis.api.data.framework;

import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.api.data.pathway.PathwayData;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class FrameworkNode implements IFrameworkNode
{
	private final IFrameworkNode schedule;


	private final boolean isNullAllowed = false;

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
	public List<BlueprintData> possibleValues(Random random)
	{
		final List<BlueprintData> superPossibleValues = this.schedule.possibleValues(random);
		if (this.isNullAllowed && !superPossibleValues.contains(null))
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

}
