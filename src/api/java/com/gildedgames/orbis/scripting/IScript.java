package com.gildedgames.orbis.scripting;

import java.util.Optional;

public interface IScript
{
	String getScriptContents();

	<T> Optional<T> eval(IScriptPars pars);
}
