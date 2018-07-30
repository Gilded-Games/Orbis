package com.gildedgames.orbis.client.gui.framework;

import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanillaToggled;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import net.minecraft.client.Minecraft;

import java.io.IOException;

public class GuiEditFramework extends GuiViewer
{
	private final Framework framework;

	private GuiButtonVanilla saveButton, closeButton;

	public GuiEditFramework(GuiViewer prevFrame, final Framework framework)
	{
		super(new GuiElement(Dim2D.flush(), false), prevFrame);

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
	public void build(IGuiContext context)
	{
		this.saveButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(75).flush());

		this.saveButton.getInner().displayString = "Save As";

		this.closeButton = new GuiButtonVanilla(Dim2D.build().width(50).height(20).addY(15).addX(20).flush());

		this.closeButton.getInner().displayString = "Close";

		context.addChildren(this.saveButton);
		context.addChildren(this.closeButton);

		if (!OrbisCore.getProjectManager().getLocation().exists())
		{
			if (!OrbisCore.getProjectManager().getLocation().mkdirs())
			{
				throw new RuntimeException("Project manager file could not be created!");
			}
		}
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (this.closeButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPreviousViewer().getActualScreen());
			GuiRightClickElements.lastCloseTime = System.currentTimeMillis();
		}

		if (this.saveButton.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GuiSaveData(this, this.framework, FrameworkData.EXTENSION));
		}
	}
}
