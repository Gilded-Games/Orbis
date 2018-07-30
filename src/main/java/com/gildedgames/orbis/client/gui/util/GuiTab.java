package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.client.gui.util.GuiTexture;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Rect;
import net.minecraft.util.ResourceLocation;

public class GuiTab extends GuiElement
{
	private static ResourceLocation TEXTURE = OrbisCore.getResource("generic/tab.png");

	private static ResourceLocation TEXTURE_PRESSED = OrbisCore.getResource("generic/tab_pressed.png");

	private GuiTexture tab_bg;

	private GuiTexture icon;

	private boolean isPressed;

	private Runnable onPressed;

	public GuiTab(Rect rect, GuiTexture icon, Runnable onPressed)
	{
		super(rect, true);

		this.dim().mod().width(22).height(19).flush();
		this.icon = icon;
		this.onPressed = onPressed;
	}

	public void setPressed(boolean flag)
	{
		this.isPressed = flag;
	}

	@Override
	public void build()
	{
		this.tab_bg = new GuiTexture(Dim2D.build().width(22).height(19).flush(), this.isPressed ? TEXTURE_PRESSED : TEXTURE);
		this.icon.dim().mod().x(this.dim().width() / 2).y(this.dim().height() / 2).addY(-2).center(true).flush();

		this.context().addChildren(this.tab_bg, this.icon);
	}

	@Override
	public void onMouseClicked(GuiElement element, final int mouseX, final int mouseY, final int mouseButton)
	{
		if (this.tab_bg.state().isHovered())
		{
			this.onPressed.run();
		}
	}
}
