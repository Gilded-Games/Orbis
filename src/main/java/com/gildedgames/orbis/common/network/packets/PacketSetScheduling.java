package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSetScheduling implements IMessage
{
	private boolean scheduling;

	public PacketSetScheduling()
	{

	}

	public PacketSetScheduling(boolean scheduling)
	{
		this.scheduling = scheduling;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.scheduling = buf.readBoolean();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeBoolean(this.scheduling);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSetScheduling, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetScheduling message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.powers().setScheduling(message.scheduling);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetScheduling, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetScheduling message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.powers().setScheduling(message.scheduling);
			}

			return null;
		}
	}
}
