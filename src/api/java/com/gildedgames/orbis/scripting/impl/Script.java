package com.gildedgames.orbis.scripting.impl;

import com.gildedgames.orbis.scripting.IScript;
import com.gildedgames.orbis.scripting.IScriptPars;
import com.gildedgames.orbis.scripting.IScriptingEngine;

import java.util.Optional;

public class Script implements IScript
{
	private IScriptingEngine engine;

	private IScriptPars pars;

	private String script;

	public Script(IScriptingEngine engine, String script)
	{
		this.engine = engine;
		this.script = script;
	}

	@Override
	public String getScriptContents()
	{
		return this.script;
	}

	@Override
	public Optional<Object> eval(IScriptPars pars)
	{
		this.pars = pars;

		return this.engine.eval(this.script, this.pars);
	}
}
