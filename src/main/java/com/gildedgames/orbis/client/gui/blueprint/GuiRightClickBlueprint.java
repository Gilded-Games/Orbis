package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.client.gui.GuiLayerEditor;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiRightClickBlueprint extends GuiFrame
{
	private final Blueprint blueprint;

	public GuiRightClickBlueprint(final Blueprint blueprint)
	{
		super(null, Dim2D.flush());

		this.blueprint = blueprint;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.addChildren(new GuiDropdownList(Pos2D.flush(this.width / 2, this.height / 2),
				new DropdownElement(new TextComponentString("Edit"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						Minecraft.getMinecraft().displayGuiScreen(new GuiLayerEditor(GuiRightClickBlueprint.this.blueprint));
					}
				},
				GuiRightClickElements.remove(this.blueprint),
				GuiRightClickElements.fillWithVoid(this.blueprint),
				GuiRightClickElements.copy(this.blueprint),
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
