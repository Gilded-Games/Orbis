package com.gildedgames.orbis.client.gui.right_click;

import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.data.IDropdownElement;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.data.pathway.Entrance;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class GuiRightClickEntrance extends GuiViewer
{
	private final Blueprint blueprint;

	private final Entrance entrance;

	public GuiRightClickEntrance(Blueprint blueprint, final Entrance entrance)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.blueprint = blueprint;
		this.entrance = entrance;
	}

	@Override
	public void build(IGuiContext context)
	{
		context.addChildren(new GuiDropdownList<IDropdownElement>(Dim2D.build().pos(this.width / 2, this.height / 2).width(70).flush(),
				GuiRightClickElements.remove(this.blueprint, this.entrance),
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
