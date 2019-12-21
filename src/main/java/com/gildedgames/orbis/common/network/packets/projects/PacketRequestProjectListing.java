package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.network.util.PacketMultipleParts;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRequestProjectListing extends PacketMultipleParts
{

	public PacketRequestProjectListing()
	{

	}

	private PacketRequestProjectListing(final byte[] data)
	{
		super(data);
	}

	@Override
	public void read(final ByteBuf buf)
	{

	}

	@Override
	public void write(final ByteBuf buf)
	{

	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketRequestProjectListing(data);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketRequestProjectListing, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRequestProjectListing message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			OrbisCore.network().sendPacketToPlayer(new PacketSendProjectListing(), (EntityPlayerMP) player);

			return null;
		}
	}
}
