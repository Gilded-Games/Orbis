package com.gildedgames.orbis.client;

import com.gildedgames.orbis.client.gui.util.GuiCombineButton;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.PacketOpenGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class CombineMatrixButtonEvents
{
	private static int BUTTON_ID;

	@SubscribeEvent
	public static void onEvent(GuiScreenEvent.InitGuiEvent.Post event)
	{
		GuiScreen gui = event.getGui();

		if (gui instanceof GuiContainerCreative && PlayerOrbis.get(Minecraft.getMinecraft().player).inDeveloperMode())
		{
			int max = event.getButtonList().stream().mapToInt((button) -> button.id).max().orElse(-1);

			BUTTON_ID = max + 1;

			event.getButtonList().add(new GuiCombineButton(BUTTON_ID, gui.width / 2 + 48, gui.height / 2 + 70, 20, 20, ""));
		}
	}

	@SubscribeEvent
	public static void onEvent(GuiScreenEvent.ActionPerformedEvent event)
	{
		GuiScreen gui = event.getGui();

		if (gui instanceof GuiContainerCreative && PlayerOrbis.get(Minecraft.getMinecraft().player).inDeveloperMode())
		{
			if (event.getButton() != null && event.getButton().id == BUTTON_ID)
			{
				EntityPlayer player = Minecraft.getMinecraft().player;

				OrbisCore.network().sendPacketToServer(
						new PacketOpenGui(OrbisGuiHandler.COMBINE_MATRIX, player.getPosition().getX(), player.getPosition().getY(),
								player.getPosition().getZ()));
			}
		}
	}
}
