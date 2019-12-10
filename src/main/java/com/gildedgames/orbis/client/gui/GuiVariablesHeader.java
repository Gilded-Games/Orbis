package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.client.gui.data.Text;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdown;
import com.gildedgames.orbis.lib.client.gui.util.GuiText;
import com.gildedgames.orbis.lib.client.gui.util.GuiTexture;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.client.rect.Rect;
import com.gildedgames.orbis.lib.client.rect.RectModifier;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nullable;

public class GuiVariablesHeader extends GuiElement
{
	private static final ResourceLocation VARIABLE_HEADER = OrbisCore.getResource("layer_gui/variable_header.png");

	private static final ResourceLocation VARIABLE_HEADER_EXTENDED = OrbisCore.getResource("layer_gui/variable_header_extended.png");

	private GuiTexture window;

	private GuiText title, type;

	private GuiDropdown dropdown;

	public GuiVariablesHeader(Rect rect)
	{
		super(rect, false);
	}

	public void setTitle(ITextComponent text)
	{
		this.setTitleAndDropdown(text, null);
	}

	public void setTitleAndDropdown(ITextComponent text, @Nullable GuiDropdown dropdown)
	{
		this.tryRebuild();

		this.window.setResourceLocation(dropdown == null ? VARIABLE_HEADER : VARIABLE_HEADER_EXTENDED, 200, dropdown == null ? 29 : 49);
		this.title.setText(new Text(text, 1.0F));
		this.type.state().setVisible(dropdown != null);

		if (dropdown != null)
		{
			this.dropdown = dropdown;

			this.dropdown.state().setEnabled(true);
			this.dropdown.state().setVisible(true);

			this.dropdown.dim().mod().x(40).y(25).flush();

			this.context().addChildren(this.dropdown);
		}
	}

	@Override
	public void build()
	{
		if (this.window == null)
		{
			this.window = new GuiTexture(Dim2D.build().width(200).height(29).flush(), VARIABLE_HEADER);
		}

		if (this.title == null)
		{
			this.title = new GuiText(Dim2D.build().x(12).y(10).flush(),
					new Text(new TextComponentString(""), 1.0F));
		}

		if (this.type == null)
		{
			this.type = new GuiText(Dim2D.build().x(8).y(30).flush(),
					new Text(new TextComponentTranslation("orbis.gui.type"), 1.0F));

			this.type.state().setVisible(false);
		}

		if (!this.dim().containsModifier("windowArea", this.window))
		{
			this.dim().add(new RectModifier("windowArea", this.window, RectModifier.ModifierType.AREA.getModification(), RectModifier.ModifierType.AREA));
		}

		this.context().addChildren(this.window, this.title, this.type);
	}
}