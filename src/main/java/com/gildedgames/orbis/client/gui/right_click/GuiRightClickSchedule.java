package com.gildedgames.orbis.client.gui.right_click;

import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.data.IDropdownElement;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
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

		this.addChildren(new GuiDropdownList<IDropdownElement>(Dim2D.build().pos(this.width / 2, this.height / 2).width(60).flush(),
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
