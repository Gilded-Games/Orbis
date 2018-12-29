package com.gildedgames.orbis.scripting;

import java.util.Optional;

public interface IScriptEvaluable
{
	Optional<Object> get(String variable);
}
