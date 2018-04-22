package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.util.InputHelper;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiTickBox extends GuiFrame
{
	private static final ResourceLocation TICK_BOX = OrbisCore.getResource("blueprint_gui/tick_box.png");

	private static final ResourceLocation TICK_BOX_PRESSED = OrbisCore.getResource("blueprint_gui/tick_box_pressed.png");

	private boolean ticked;

	private GuiTexture pressed;

	public GuiTickBox(Pos2D pos, boolean ticked)
	{
		super(Dim2D.build().width(14).height(14).pos(pos).flush());

		this.setTicked(ticked);
	}

	public boolean isTicked()
	{
		return this.ticked;
	}

	public void setTicked(boolean ticked)
	{
		this.ticked = ticked;
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this) && mouseButton == 0)
		{
			this.ticked = !this.ticked;
		}
	}

	@Override
	public void init()
	{
		GuiTexture box = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TICK_BOX);
		this.pressed = new GuiTexture(Dim2D.build().width(14).height(14).flush(), TICK_BOX_PRESSED);

		this.addChildren(box, this.pressed);
	}

	@Override
	public void draw()
	{
		this.pressed.setVisible(this.ticked);
	}
}
