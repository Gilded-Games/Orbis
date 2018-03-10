package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.client.gui.data.Text;
import com.gildedgames.orbis.client.gui.util.GuiButtonVanilla;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.gui.util.GuiText;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.util.InputHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.File;
import java.io.IOException;

public class GuiSaveCallback extends GuiFrame
{
	private GuiButtonVanilla yes, no;

	private GuiText warning;

	private File file;

	private String location;

	public GuiSaveCallback(GuiFrame prevFrame, File file, String location)
	{
		super(prevFrame, Dim2D.flush());

		this.file = file;
		this.location = location;

		this.setDrawDefaultBackground(true);
	}

	@Override
	public void initGui()
	{
		super.initGui();
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		Pos2D center = InputHelper.getCenter();

		this.yes = new GuiButtonVanilla(Dim2D.build().width(80).height(20).pos(center).addX(-85).addY(5).flush());
		this.no = new GuiButtonVanilla(Dim2D.build().width(80).height(20).pos(center).addX(5).addY(5).flush());

		this.yes.getInner().displayString = "Yes";
		this.no.getInner().displayString = "No";

		this.warning = new GuiText(Dim2D.build().center(true).pos(center).addY(-15).flush(),
				new Text(new TextComponentTranslation("You're about to overwrite an existing file. Are you sure?"), 1.0F));

		this.addChildren(this.yes, this.no, this.warning);
	}

	@Override
	protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);

		if (InputHelper.isHovered(this.yes) && mouseButton == 0)
		{
			if (this.getPrevFrame() instanceof GuiSaveBlueprint)
			{
				GuiSaveBlueprint save = (GuiSaveBlueprint) this.getPrevFrame();

				save.save(this.file, this.location, true);

				Minecraft.getMinecraft().displayGuiScreen(save);
			}
		}

		if (InputHelper.isHovered(this.no) && mouseButton == 0)
		{
			Minecraft.getMinecraft().displayGuiScreen(this.getPrevFrame());
		}
	}
}
