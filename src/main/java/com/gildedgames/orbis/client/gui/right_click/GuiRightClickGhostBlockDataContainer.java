package com.gildedgames.orbis.client.gui.right_click;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.GhostBlockDataContainer;
import com.gildedgames.orbis.lib.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class GuiRightClickGhostBlockDataContainer extends GuiViewer
{
	private final GhostBlockDataContainer data;

	public GuiRightClickGhostBlockDataContainer(PlayerOrbis playerOrbis, final GhostBlockDataContainer data)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.setDrawDefaultBackground(false);

		this.data = data;
	}

	@Override
	public void build(IGuiContext context)
	{
		context.addChildren(new GuiDropdownList<IDropdownElement>(Dim2D.build().pos(this.width / 2, this.height / 2).width(70).flush(),
				GuiRightClickElements.generate(this.data),
				GuiRightClickElements.remove(this.data),
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
