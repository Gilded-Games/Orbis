package com.gildedgames.orbis.client.gui.framework;

import com.gildedgames.orbis.client.gui.right_click.GuiRightClickElements;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.client.gui.data.DropdownElement;
import com.gildedgames.orbis_api.client.gui.data.IDropdownElement;
import com.gildedgames.orbis_api.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis_api.client.gui.util.GuiFrame;
import com.gildedgames.orbis_api.client.rect.Dim2D;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiRightClickFramework extends GuiFrame
{
	private final Framework framework;

	public GuiRightClickFramework(final Framework framework)
	{
		super(null, Dim2D.flush());

		this.framework = framework;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.addChildren(new GuiDropdownList<IDropdownElement>(Dim2D.build().pos(this.width / 2, this.height / 2).width(70).flush(),
				new DropdownElement(new TextComponentString("Edit"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						Minecraft.getMinecraft().displayGuiScreen(new GuiEditFramework(null, GuiRightClickFramework.this.framework));
					}
				},
				GuiRightClickElements.remove(this.framework),
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
