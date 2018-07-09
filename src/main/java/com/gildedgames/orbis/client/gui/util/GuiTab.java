package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import com.gildedgames.orbis_api.util.InputHelper;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class GuiTab extends GuiFrame
{
	private static ResourceLocation TEXTURE = OrbisCore.getResource("generic/tab.png");

	private static ResourceLocation TEXTURE_PRESSED = OrbisCore.getResource("generic/tab_pressed.png");

	private GuiTexture tab_bg;

	private GuiTexture icon;

	private boolean isPressed;

	private Runnable onPressed;

	public GuiTab(Rect rect, GuiTexture icon, Runnable onPressed)
	{
		super(null, rect);

		this.dim().mod().width(22).height(19).flush();
		this.icon = icon;
		this.onPressed = onPressed;
	}

	public void setPressed(boolean flag)
	{
		this.isPressed = flag;
	}

	@Override
	public void init()
	{
		this.tab_bg = new GuiTexture(Dim2D.build().width(22).height(19).flush(), this.isPressed ? TEXTURE_PRESSED : TEXTURE);
		this.icon.dim().mod().x(this.dim().width() / 2).y(this.dim().height() / 2).addY(-2).center(true).flush();

		this.addChildren(this.tab_bg, this.icon);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHoveredAndTopElement(this.tab_bg))
		{
			this.onPressed.run();
		}
	}
}
