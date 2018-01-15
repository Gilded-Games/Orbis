package com.gildedgames.orbis.api.data.schedules;

public enum ScheduleDataType
{

	FILL("Fill"), DELETE("Delete"), REPLACE("Replace"), BLUEPRINT("Blueprint");

	String name;

	ScheduleDataType(final String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

}
