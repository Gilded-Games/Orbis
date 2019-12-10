package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.OrbisLib;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketWorldObjectRemove implements IMessage
{

	private int objectId, dimensionId;

	public PacketWorldObjectRemove()
	{

	}

	public PacketWorldObjectRemove(final int objectId, final int dimensionId)
	{
		this.objectId = objectId;
		this.dimensionId = dimensionId;
	}

	public PacketWorldObjectRemove(final World world, final IWorldObject object)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);

		this.objectId = manager.getID(object);
		this.dimensionId = object.getWorld().provider.getDimension();
	}

	public static void onMessage(final PacketWorldObjectRemove message, final EntityPlayer player)
	{
		//TODO: This assumes the player sending this message is in the world we want to put the World Object
		//Clients cannot send a packet requestion a change in a different dimension.
		final WorldObjectManager manager = WorldObjectManager.get(player.world);

		manager.removeObject(message.objectId);
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		this.objectId = buf.readInt();
		this.dimensionId = buf.readInt();
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		buf.writeInt(this.objectId);
		buf.writeInt(this.dimensionId);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketWorldObjectRemove, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketWorldObjectRemove message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			PacketWorldObjectRemove.onMessage(message, player);

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketWorldObjectRemove, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketWorldObjectRemove message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			PacketWorldObjectRemove.onMessage(message, player);

			OrbisCore.network()
					.sendPacketToDimension(new PacketWorldObjectRemove(message.objectId, message.dimensionId), message.dimensionId);

			return null;
		}
	}
}
