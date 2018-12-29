package com.gildedgames.orbis.scripting;

import java.io.Reader;

public interface IScriptCompilable
{
	IScript compile(String script);

	IScript compile(Reader reader);
}
