package com.gildedgames.orbis.client.gui.right_click;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.PacketOpenGui;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.client.gui.data.DropdownElement;
import com.gildedgames.orbis.lib.client.gui.data.IDropdownElement;
import com.gildedgames.orbis.lib.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiElement;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.GuiViewer;
import com.gildedgames.orbis.lib.client.gui.util.gui_library.IGuiContext;
import com.gildedgames.orbis.lib.client.rect.Dim2D;
import com.gildedgames.orbis.lib.data.framework.interfaces.EnumFacingMultiple;
import com.gildedgames.orbis.lib.data.pathway.Entrance;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;
import java.util.List;

public class GuiRightClickEntrance extends GuiViewer
{
	private final Blueprint blueprint;

	private final Entrance entrance;

	public GuiRightClickEntrance(Blueprint blueprint, final Entrance entrance)
	{
		super(new GuiElement(Dim2D.flush(), false), null);

		this.setDrawDefaultBackground(false);

		this.blueprint = blueprint;
		this.entrance = entrance;
	}

	public DropdownElement createFromFace(String name, EnumFacing facing)
	{
		EnumFacingMultiple orig = GuiRightClickEntrance.this.entrance.getFacing();

		return new DropdownElement(new TextComponentString((orig.getFacings().contains(facing) ? Character.toString((char) 2713) : "") + name))
		{
			@Override
			public void onClick(final GuiDropdownList list, final EntityPlayer player)
			{
				EnumFacingMultiple orig = GuiRightClickEntrance.this.entrance.getFacing();

				List<EnumFacing> facings = Lists.newArrayList(orig.getFacings());

				if (orig.getFacings().contains(facing.getOpposite()))
				{
					facings.add(facing);
					facings.remove(facing.getOpposite());
				}
				else if (orig.getFacings().contains(facing))
				{
					if (orig.getFacings().size() > 1)
					{
						facings.remove(facing);
					}
				}
				else
				{
					facings.add(facing);
				}

				GuiRightClickEntrance.this.entrance.setFacing(EnumFacingMultiple.getFromMultiple(facings.toArray(new EnumFacing[0])));
			}
		};
	}

	@Override
	public void build(IGuiContext context)
	{
		context.addChildren(new GuiDropdownList<IDropdownElement>(Dim2D.build().pos(this.width / 2, this.height / 2).width(90).flush(),
				new DropdownElement(new TextComponentString("Edit"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						BlockPos pos = GuiRightClickEntrance.this.blueprint.getPos().add(
								GuiRightClickEntrance.this.entrance.getBounds().getMin());

						OrbisCore.network().sendPacketToServer(new PacketOpenGui(OrbisGuiHandler.EDIT_ENTRANCE, pos.getX(), pos.getY(), pos.getZ()));
					}
				},
				new DropdownElement(new TextComponentString("Select Rotation"), () ->
						new GuiDropdownList<>(Dim2D.flush(),
								GuiRightClickEntrance.this.createFromFace("North", EnumFacing.NORTH),
								GuiRightClickEntrance.this.createFromFace("East", EnumFacing.EAST),
								GuiRightClickEntrance.this.createFromFace("South", EnumFacing.SOUTH),
								GuiRightClickEntrance.this.createFromFace("West", EnumFacing.WEST),
								GuiRightClickEntrance.this.createFromFace("Up", EnumFacing.UP),
								GuiRightClickEntrance.this.createFromFace("Down", EnumFacing.DOWN)))
				{

				},
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
