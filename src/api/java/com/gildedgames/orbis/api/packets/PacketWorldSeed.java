package com.gildedgames.orbis.api.packets;

import com.gildedgames.orbis.api.packets.instances.MessageHandlerClient;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketWorldSeed implements IMessage
{

	private long seed;

	private int dimension;

	public PacketWorldSeed()
	{

	}

	public PacketWorldSeed(int dimension, long seed)
	{
		this.seed = seed;
		this.dimension = dimension;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.dimension = buf.readInt();
		this.seed = buf.readLong();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.dimension);
		buf.writeLong(this.seed);
	}

	public static class Handler extends MessageHandlerClient<PacketWorldSeed, PacketWorldSeed>
	{
		@Override
		public PacketWorldSeed onMessage(final PacketWorldSeed message, final EntityPlayer player)
		{
			WorldObjectManager.setWorldSeed(message.dimension, message.seed);

			return null;
		}
	}

}
