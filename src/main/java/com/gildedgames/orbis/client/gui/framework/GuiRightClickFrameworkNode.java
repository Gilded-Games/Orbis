package com.gildedgames.orbis.client.gui.framework;

import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis.common.world_objects.Framework;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class GuiRightClickFrameworkNode extends GuiFrame
{
	private final IFrameworkNode node;

	private Framework framework;

	public GuiRightClickFrameworkNode(Framework framework, final IFrameworkNode node)
	{
		super(null, Dim2D.flush());

		this.framework = framework;
		this.node = node;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.addChildren(new GuiDropdownList(Pos2D.flush(this.width / 2, this.height / 2),
				GuiRightClickElements.remove(this.framework, this.node),
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
