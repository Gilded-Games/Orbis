package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.data.schedules.ISchedule;
import com.gildedgames.orbis.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class GuiRightClickSchedule extends GuiFrame
{
	private final Blueprint blueprint;

	private final ISchedule schedule;

	public GuiRightClickSchedule(Blueprint blueprint, final ISchedule schedule)
	{
		super(null, Dim2D.flush());

		this.blueprint = blueprint;
		this.schedule = schedule;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.addChildren(new GuiDropdownList(Pos2D.flush(this.width / 2, this.height / 2),
				GuiRightClickElements.remove(this.blueprint, this.schedule),
				GuiRightClickElements.close()));
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (Minecraft.getMinecraft().currentScreen == this)
		{
			Minecraft.getMinecraft().displayGuiScreen(null);
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}
	}
}
