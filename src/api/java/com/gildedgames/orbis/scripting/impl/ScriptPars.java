package com.gildedgames.orbis.scripting.impl;

import com.gildedgames.orbis.scripting.IScriptPars;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ScriptPars implements IScriptPars
{
	private Map<String, Object> parToValue = Maps.newHashMap();

	public ScriptPars()
	{

	}

	@Override
	public Collection<Map.Entry<String, Object>> getEntries()
	{
		return this.parToValue.entrySet();
	}

	@Override
	public void put(String parameter, Object value)
	{
		if (parameter == null)
		{
			throw new IllegalArgumentException("Can't have null parameter when using put() for ScriptPars");
		}

		this.parToValue.put(parameter, value);
	}

	@Override
	public Optional<Object> getValue(String parameter)
	{
		return Optional.ofNullable(this.parToValue.get(parameter));
	}
}
