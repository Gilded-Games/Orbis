package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketOpenPowerGui implements IMessage
{

	public PacketOpenPowerGui()
	{

	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
	}

	public static class HandlerServer extends MessageHandlerServer<PacketOpenPowerGui, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketOpenPowerGui message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode() && playerOrbis.powers().getCurrentPower().hasCustomGui(playerOrbis))
			{
				playerOrbis.powers().getCurrentPower().onOpenGui(player);
			}

			return null;
		}
	}
}
