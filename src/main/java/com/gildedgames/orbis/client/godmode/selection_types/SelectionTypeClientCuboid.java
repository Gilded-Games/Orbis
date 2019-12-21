package com.gildedgames.orbis.client.godmode.selection_types;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import net.minecraft.util.ResourceLocation;

public class SelectionTypeClientCuboid implements ISelectionTypeClient
{
	private static final ResourceLocation TEXTURE = OrbisCore.getResource("godmode/shape_icons/cuboid_icon.png");

	private final GuiTexture icon;

	public SelectionTypeClientCuboid()
	{
		this.icon = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TEXTURE);
	}

	@Override
	public String displayName()
	{
		return "Cuboid";
	}

	@Override
	public GuiTexture getIcon()
	{
		return this.icon;
	}
}
