package com.gildedgames.orbis.api.data.schedules;

public enum ScheduleDataType
{

	DATA("Data");

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
