package com.gildedgames.orbis.client.gui.util;

import com.gildedgames.orbis.common.OrbisCore;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiCombineButton extends GuiButton
{
	protected static final ResourceLocation ICON = OrbisCore.getResource("godmode/placement_icons/replace_icon.png");

	private List<String> tooltips = Lists.newArrayList();

	public GuiCombineButton(int buttonId, int x, int y, String buttonText)
	{
		super(buttonId, x, y, buttonText);
	}

	public GuiCombineButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
	{
		super(buttonId, x, y, widthIn, heightIn, buttonText);

		this.tooltips.add(I18n.format("orbis.gui.combine_blocks"));
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);

		if (this.visible)
		{
			mc.getTextureManager().bindTexture(ICON);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.enableBlend();
			GlStateManager
					.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
							GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			Gui.drawModalRectWithCustomSizedTexture(this.x + 2, this.y + 2, 0, 0, 16, 16, 16, 16);

			if (this.hovered)
			{
				Minecraft.getMinecraft().currentScreen.drawHoveringText(this.tooltips, mouseX, mouseY);
			}
		}
	}

}
