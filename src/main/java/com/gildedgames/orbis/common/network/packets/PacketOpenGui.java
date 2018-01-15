package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketOpenGui implements IMessage
{

	public PacketOpenGui()
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

	public static class HandlerServer extends MessageHandlerServer<PacketOpenGui, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketOpenGui message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode() && playerOrbis.powers().getCurrentPower().hasCustomGui())
			{
				playerOrbis.powers().getCurrentPower().onOpenGui(player);
			}

			return null;
		}
	}
}
