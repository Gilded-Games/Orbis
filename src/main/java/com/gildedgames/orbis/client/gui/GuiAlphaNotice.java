package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.client.gui.data.Text;
import com.gildedgames.orbis.lib.client.gui.util.GuiText;
import com.gildedgames.orbis.lib.client.gui.util.GuiTextBox;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewerNoContainer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.RectModifier;
import com.gildedgames.orbis.lib.util.InputHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiAlphaNotice extends GuiViewerNoContainer
{
	private static final ResourceLocation LOGO = OrbisCore.getResource("full_logo.png");

	private GuiTexture orbisLogo;

	private GuiTextBox notice;

	private GuiButtonVanilla gotit;

	public GuiAlphaNotice()
	{
		super(new GuiElement(Dim2D.flush(), true), null);

		this.setDrawDefaultBackground(false);
	}

	@Override
	public void build(IGuiContext context)
	{
		this.getViewing().dim().mod().width(this.width).height(this.height).flush();

		RectModifier centerX = new RectModifier("centerX", null,
				(source, modifying) -> this.width / 2,
				RectModifier.ModifierType.X);

		GuiText title = new GuiText(Dim2D.build().center(true).flush(),
				new Text(new TextComponentTranslation("orbis.gui.alpha_notice_title"), 1.0F));

		this.orbisLogo = new GuiTexture(Dim2D.build().width(512).height(92).center(true).scale(0.5F).flush(), LOGO);

		this.notice = new GuiTextBox(Dim2D.build().width(380).addY(30).center(true).flush(), false,
				new Text(new TextComponentTranslation("orbis.gui.alpha_notice"), 1.0F));

		this.gotit = new GuiButtonVanilla(Dim2D.build().center(true).width(100).height(20).y(-15).flush());

		this.gotit.getInner().displayString = I18n.format("orbis.gui.gotit");

		this.orbisLogo.dim()
				.add(new RectModifier("inbetweenTextboxAndTop", this.notice,
						(source, modifying) -> source.dim().min().y() / 2,
						RectModifier.ModifierType.Y));

		title.dim().add(new RectModifier("inbetweenTextboxAndTop", this.notice,
				(source, modifying) -> (source.dim().min().y() / 2) + 40,
				RectModifier.ModifierType.Y));

		this.notice.dim().add(new RectModifier("centerY", null,
				(source, modifying) -> this.height / 2,
				RectModifier.ModifierType.Y));

		this.gotit.dim().add(new RectModifier("centerY", this.notice,
				(source, modifying) -> source.dim().maxY() + ((InputHelper.getScreenHeight() - source.dim().maxY()) / 2),
				RectModifier.ModifierType.Y));

		this.orbisLogo.dim().add(centerX);
		title.dim().add(centerX);
		this.notice.dim().add(centerX);
		this.gotit.dim().add(centerX);

		context.addChildren(this.orbisLogo, title, this.notice, this.gotit);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		final int bg = 0xFF23333a;

		this.drawGradientRect(0, 0, this.width, this.height, bg, bg);

		super.drawScreen(mouseX, mouseY, partialTicks);

		preventInnerTyping();
	}

	@Override
	protected void mouseReleased(final int mouseX, final int mouseY, final int state)
	{
		super.mouseReleased(mouseX, mouseY, state);

		if (InputHelper.isHovered(this.gotit) && state == 0)
		{
			OrbisCore.CONFIG.markSeenAlphaNotice();
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}
}
