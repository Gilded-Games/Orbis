package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.client.gui.data.DropdownElement;
import com.gildedgames.orbis.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.PacketOpenGui;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiRightClickScheduleRegion extends GuiFrame
{
	private final Blueprint blueprint;

	private final ScheduleRegion schedule;

	public GuiRightClickScheduleRegion(Blueprint blueprint, final ScheduleRegion schedule)
	{
		super(null, Dim2D.flush());

		this.blueprint = blueprint;
		this.schedule = schedule;
	}

	@Override
	public void init()
	{
		this.addChildren(new GuiDropdownList(Pos2D.flush(this.width / 2, this.height / 2),
				new DropdownElement(new TextComponentString("Edit"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						BlockPos pos = GuiRightClickScheduleRegion.this.blueprint.getPos().add(
								GuiRightClickScheduleRegion.this.schedule.getBounds().getMin());

						OrbisAPI.network().sendPacketToServer(new PacketOpenGui(OrbisGuiHandler.EDIT_SCHEDULE_REGION, pos.getX(), pos.getY(), pos.getZ()));
					}
				},
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
