package com.gildedgames.orbis.client.gui.schedules;

import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.util.*;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Rect;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketSetScheduleLayerInfo;
import com.gildedgames.orbis.common.util.InputHelper;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiScheduleLayerPanel extends GuiFrame
{

	private Blueprint blueprint;

	private IScheduleLayer layer;

	private GuiText title;

	private GuiInput nameInput;

	private GuiButtonVanilla saveButton, resetButton;

	private GuiInputSlider noise;

	private boolean setText;

	public GuiScheduleLayerPanel(final Rect rect, Blueprint blueprint, IScheduleLayer layer)
	{
		super(rect);

		this.blueprint = blueprint;
		this.layer = layer;
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (mouseButton == 0)
		{
			if (InputHelper.isHovered(this.saveButton))
			{
				NetworkingOrbis
						.sendPacketToServer(
								new PacketSetScheduleLayerInfo(this.blueprint, this.layer, this.nameInput.getInner().getText(), this.noise.getSliderValue()));
			}
			else if (InputHelper.isHovered(this.resetButton))
			{
				this.nameInput.getInner().setText(this.layer.getDisplayName());
				this.noise.setSliderValue(this.layer.getEdgeNoise());
			}
		}
	}

	@Override
	public void init()
	{
		this.title = new GuiText(Dim2D.build().width(140).height(20).addY(20).addX(20).flush(),
				new Text(new TextComponentString("Layer Name:"), 1.0F));

		this.nameInput = new GuiInput(Dim2D.build().height(20).addY(35).addX(20).flush());

		this.nameInput.dim().mod().width(this.dim().width() - 40).flush();

		this.saveButton = new GuiButtonVanilla(
				Dim2D.build().width(80).height(20).addY(this.dim().height() - 30).addX((this.dim().width() / 2) - 85).flush());

		this.saveButton.getInner().displayString = "Save Changes";

		this.resetButton = new GuiButtonVanilla(
				Dim2D.build().width(80).height(20).addY(this.dim().height() - 30).addX((this.dim().width() / 2) + 5).flush());

		this.resetButton.getInner().displayString = "Reset Changes";

		GuiText noiseTitle = new GuiText(Dim2D.build().width(140).height(20).addY(62).addX(20).flush(),
				new Text(new TextComponentString("Edge Noise:"), 1.0F));

		this.noise = new GuiInputSlider(Dim2D.build().height(20).width(60).flush(), 0, 100, 1.0F);

		this.noise.dim().mod().width(this.dim().width() - 40).x(20).y(75).flush();

		this.noise.setSliderValue(this.layer.getEdgeNoise());

		this.addChildren(this.title, this.nameInput, this.saveButton, this.resetButton, this.noise, noiseTitle);
	}

	@Override
	public void draw()
	{
		super.draw();

		GuiFrame.preventInnerTyping = this.nameInput.getInner().isFocused();

		Gui.drawRect((int) this.dim().x(), (int) this.dim().y(), (int) this.dim().maxX(), (int) this.dim().maxY(), Integer.MIN_VALUE);

		if (!this.setText)
		{
			this.nameInput.getInner().setText(this.layer.getDisplayName());
			this.setText = true;
		}
	}
}
