package com.gildedgames.orbis.packs;

import java.util.Collection;

public interface IOrbisPack
{
	String getPackName();

	String getPackIconLocation();

	Collection<IOrbisPackData> getData();
}
