package com.gildedgames.orbis.client.gui.right_click;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.PacketOpenGui;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElement;
import com.gildedgames.orbis.lib.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.data.schedules.ScheduleRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiRightClickScheduleRegion extends GuiViewer
{
	private final ScheduleRegion schedule;

	private Blueprint blueprint;

	public GuiRightClickScheduleRegion(Blueprint blueprint, final ScheduleRegion schedule)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.setDrawDefaultBackground(false);

		this.blueprint = blueprint;
		this.schedule = schedule;
	}

	@Override
	public void build(IGuiContext context)
	{
		context.addChildren(new GuiDropdownList<IDropdownElement>(Dim2D.build().pos(this.width / 2, this.height / 2).width(70).flush(),
				new DropdownElement(new TextComponentString("Edit"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						BlockPos pos = GuiRightClickScheduleRegion.this.blueprint.getPos().add(
								GuiRightClickScheduleRegion.this.schedule.getBounds().getMin());

						OrbisCore.network().sendPacketToServer(new PacketOpenGui(OrbisGuiHandler.EDIT_SCHEDULE_REGION, pos.getX(), pos.getY(), pos.getZ()));
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
