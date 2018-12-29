package com.gildedgames.orbis.scripting;

import java.util.Optional;

public interface IScriptingEngine extends IScriptPars, IScriptCompilable, IScriptEvaluable
{
	Optional<Object> eval(String script);

	Optional<Object> eval(String script, IScriptPars pars);

	String getExtension();
}
