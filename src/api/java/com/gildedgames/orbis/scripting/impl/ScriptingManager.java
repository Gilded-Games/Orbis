package com.gildedgames.orbis.scripting.impl;

import com.gildedgames.orbis.scripting.IScriptingEngine;
import com.gildedgames.orbis.scripting.IScriptingManager;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Optional;

public class ScriptingManager implements IScriptingManager
{
	private Map<String, IScriptingEngine> extensionToEngine = Maps.newHashMap();

	@Override
	public Optional<IScriptingEngine> getEngineFromExtension(String extension)
	{
		return Optional.ofNullable(this.extensionToEngine.get(extension));
	}

	@Override
	public void register(IScriptingEngine engine)
	{
		this.extensionToEngine.put(engine.getExtension(), engine);
	}
}
