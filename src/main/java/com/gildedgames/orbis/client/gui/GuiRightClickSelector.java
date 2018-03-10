package com.gildedgames.orbis.client.gui;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.client.gui.data.DropdownElement;
import com.gildedgames.orbis.client.gui.util.GuiDropdownList;
import com.gildedgames.orbis.client.gui.util.GuiFrame;
import com.gildedgames.orbis.client.rect.Dim2D;
import com.gildedgames.orbis.client.rect.Pos2D;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketClearSelectedRegion;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

public class GuiRightClickSelector extends GuiFrame
{
	private final WorldShape region;

	private PlayerOrbis playerOrbis;

	public GuiRightClickSelector(PlayerOrbis playerOrbis, final WorldShape region)
	{
		super(null, Dim2D.flush());

		this.playerOrbis = playerOrbis;
		this.region = region;
	}

	@Override
	public void init()
	{
		this.dim().mod().width(this.width).height(this.height).flush();

		this.addChildren(new GuiDropdownList(Pos2D.flush(this.width / 2, this.height / 2),
				GuiRightClickElements.delete(this.region),
				GuiRightClickElements.copy(this.region),
				new DropdownElement(new TextComponentString("Remove"))
				{
					@Override
					public void onClick(final GuiDropdownList list, final EntityPlayer player)
					{
						OrbisAPI.network().sendPacketToServer(new PacketClearSelectedRegion());
						OrbisAPI.network().sendPacketToServer(new PacketWorldObjectRemove(GuiRightClickSelector.this.region.getWorld(),
								GuiRightClickSelector.this.region));

						GuiRightClickSelector.this.playerOrbis.powers().getSelectPower().setSelectedRegion(null);
					}
				},
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
