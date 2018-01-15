package com.gildedgames.orbis.client.godmode.selection_types;

import com.gildedgames.orbis.client.gui.util.GuiTexture;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.util.ResourceLocation;

public class SelectionTypeClientCone implements ISelectionTypeClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/shape_icons/cone_icon.png");

	private final GuiTexture icon;

	public SelectionTypeClientCone()
	{
		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public String displayName()
	{
		return "Cone";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}
}
