package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis_api.client.gui.data.Text;
import com.gildedgames.orbis_api.client.gui.util.GuiText;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis_api.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis_api.client.gui.util.gui_library.IGuiViewer;
import com.gildedgames.orbis_api.client.gui.util.vanilla.GuiButtonVanilla;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.util.InputHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.File;
import java.io.IOException;

public class GuiSaveCallback extends GuiViewer
{
	private GuiButtonVanilla yes, no;

	private GuiText warning;

	private File file;

	private String location;

	public GuiSaveCallback(IGuiViewer prevViewer, File file, String location)
	{
		super(new GuiElement(Dim2D.flush(), false), prevViewer);

		this.file = file;
		this.location = location;

		this.setDrawDefaultBackground(true);
	}

	@Override
	public void build(IGuiContext context)
	{
		Pos2D center = InputHelper.getCenter();

		this.yes = new GuiButtonVanilla(Dim2D.build().width(80).height(20).pos(center).addX(-85).addY(5).flush());
		this.no = new GuiButtonVanilla(Dim2D.build().width(80).height(20).pos(center).addX(5).addY(5).flush());

		this.yes.getInner().displayString = "Yes";
		this.no.getInner().displayString = "No";

		this.warning = new GuiText(Dim2D.build().center(true).pos(center).addY(-15).flush(),
				new Text(new TextComponentTranslation("orbis.gui.overwrite"), 1.0F));

		context.addChildren(this.yes, this.no, this.warning);
	}

	@Override
	public void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		if (this.yes.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			if (this.getPreviousViewer() instanceof GuiSaveData)
			{
				GuiSaveData save = (GuiSaveData) this.getPreviousViewer();

				save.save(this.file, this.location, true);

				Minecraft.getMinecraft().displayGuiScreen(save);
			}
		}

		if (this.no.state().isHoveredAndTopElement() && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPreviousViewer().getActualScreen());
		}
	}
}
