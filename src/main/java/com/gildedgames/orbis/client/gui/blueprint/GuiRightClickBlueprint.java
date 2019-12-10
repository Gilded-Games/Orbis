package com.gildedgames.orbis.client.gui.blueprint;

import com.gildedgames.orbis.client.gui.GuiLayerEditor;
import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElement;
import com.gildedgames.orbis.lib.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiRightClickBlueprint extends GuiViewer
{
	private final Blueprint blueprint;

	public GuiRightClickBlueprint(final Blueprint blueprint)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.setDrawDefaultBackground(false);

		this.blueprint = blueprint;
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
