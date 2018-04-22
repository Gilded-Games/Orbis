package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.gui.util.GuiFrameUtils;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.util.InputHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

public class GuiButtonVanillaToggled extends GuiFrame
{
	public static int TOGGLED_BUTTON_ID = -1;

	private final GuiButton button;

	private int id;

	public GuiButtonVanillaToggled(final Rect rect, final int id)
	{
		super(rect);

		this.id = id;

		this.button = new GuiButton(0, (int) rect.x(), (int) rect.y(), (int) rect.width(), (int) rect.height(), "");
	}

	public int getId()
	{
		return this.id;
	}

	public void setId(final int id)
	{
		this.id = id;
	}

	public GuiButton getInner()
	{
		return this.button;
	}

	@Override
	public void init()
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		if (this.button.isMouseOver())
		{
			this.button.drawButtonForegroundLayer(mouseX - this.guiLeft, mouseY - this.guiTop);
		}
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.button.mousePressed(this.mc, mouseX, mouseY))
		{
			this.button.playPressSound(this.mc.getSoundHandler());
			this.actionPerformed(this.button);

			TOGGLED_BUTTON_ID = this.id;
		}
	}

	@Override
	public void draw()
	{
		GlStateManager.pushMatrix();

		GuiFrameUtils.applyAlpha(this);

		this.button.x = (int) this.dim().x();
		this.button.y = (int) this.dim().y();

		this.button.width = (int) this.dim().width();
		this.button.height = (int) this.dim().height();

		this.button.drawButton(Minecraft.getMinecraft(), (int) InputHelper.getMouseX(), (int) InputHelper.getMouseY(), AirSelectionRenderer.PARTIAL_TICKS);

		if (this.id == TOGGLED_BUTTON_ID)
		{
			Gui.drawRect((int) this.dim().x(), (int) this.dim().y(), (int) this.dim().maxX(), (int) this.dim().maxY(), Integer.MIN_VALUE);
		}

		GlStateManager.popMatrix();
	}
}
