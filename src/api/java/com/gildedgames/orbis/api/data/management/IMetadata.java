package com.gildedgames.orbis.api.data.management;

import com.gildedgames.orbis.api.util.mc.IText;
import com.gildedgames.orbis.api.util.mc.NBT;

import java.util.List;

public interface IMetadata extends NBT
{
	/**
	 * Used for displaying this metadata to users in the GUI.
	 * @return The text that will be displayed.
	 */
	List<IText> getMetadataDisplay();
}
