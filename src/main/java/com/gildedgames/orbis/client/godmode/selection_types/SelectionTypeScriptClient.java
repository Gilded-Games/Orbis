package com.gildedgames.orbis.client.godmode.selection_types;

import com.gildedgames.orbis.client.ISelectionTypeClient;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;

public class SelectionTypeScriptClient implements ISelectionTypeClient
{
	private String displayName;

	private GuiTexture icon;

	public SelectionTypeScriptClient(String displayName, GuiTexture icon)
	{
		this.displayName = displayName;
		this.icon = icon;
	}

	@Override
	public String displayName()
	{
		return this.displayName;
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}
}
