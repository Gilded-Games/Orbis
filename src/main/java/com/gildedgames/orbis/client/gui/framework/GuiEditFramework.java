package com.gildedgames.orbis.client.gui.framework;

import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.client.gui.util.GuiButtonVanilla;
import com.gildedgames.orbis.client.gui.util.GuiButtonVanillaToggled;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.util.InputHelper;
import com.gildedgames.orbis.common.world_objects.Framework;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class GuiEditFramework extends GuiFrame
{
	private final Framework framework;

	private GuiButtonVanilla saveButton, closeButton;

	public GuiEditFramework(GuiFrame prevFrame, final Framework framework)
	{
		super(prevFrame, Dim2D.flush());

		this.setDrawDefaultBackground(true);
		this.framework = framework;
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		GuiButtonVanillaToggled.TOGGLED_BUTTON_ID = -1;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(75).flush());

		this.saveButton.getInner().displayString = "Save As";

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(20).flush());

		this.closeButton.getInner().displayString = "Close";

		this.addChildren(this.saveButton);
		this.addChildren(this.closeButton);

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}
	}

	@Override
	public void draw()
	{
		super.draw();
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.closeButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (InputHelper.isHovered(this.saveButton) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.framework, FrameworkData.EXTENSION));
		}
	}
}
