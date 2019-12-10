package com.gildedgames.orbis.common.network.packets.creation_settings;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSetPlacesAirBlocks implements IMessage
{

	private boolean flag = true;

	public PacketSetPlacesAirBlocks()
	{

	}

	public PacketSetPlacesAirBlocks(final boolean flag)
	{
		this.flag = flag;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.flag = buf.readBoolean();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeBoolean(this.flag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSetPlacesAirBlocks, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetPlacesAirBlocks message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getCreationSettings().setPlacesAirBlocks(message.flag);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetPlacesAirBlocks, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetPlacesAirBlocks message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getCreationSettings().setPlacesAirBlocks(message.flag);
			}

			return null;
		}
	}
}
