package com.gildedgames.orbis.common.network.packets.creation_settings;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSetPlaceChunksAsGhostRegions implements IMessage
{

	private boolean flag = true;

	public PacketSetPlaceChunksAsGhostRegions()
	{

	}

	public PacketSetPlaceChunksAsGhostRegions(final boolean flag)
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

	public static class HandlerClient extends MessageHandlerClient<PacketSetPlaceChunksAsGhostRegions, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetPlaceChunksAsGhostRegions message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getCreationSettings().setPlaceChunksAsGhostRegions(message.flag);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetPlaceChunksAsGhostRegions, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetPlaceChunksAsGhostRegions message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getCreationSettings().setPlaceChunksAsGhostRegions(message.flag);
			}

			return null;
		}
	}
}
