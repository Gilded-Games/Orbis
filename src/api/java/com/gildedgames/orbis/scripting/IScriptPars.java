package com.gildedgames.orbis.scripting;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface IScriptPars
{
	Collection<Map.Entry<String, Object>> getEntries();

	void put(String parameter, Object value);

	Optional<Object> getValue(String parameter);
}
