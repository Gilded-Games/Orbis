package com.gildedgames.orbis.scripting;

import java.util.Optional;

public interface IScriptingManager
{
	Optional<IScriptingEngine> getEngineFromExtension(String extension);

	void register(IScriptingEngine engine);
}
