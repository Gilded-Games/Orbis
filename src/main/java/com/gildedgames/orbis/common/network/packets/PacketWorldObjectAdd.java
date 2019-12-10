package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.network.util.PacketMultipleParts;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketWorldObjectAdd extends PacketMultipleParts
{
	private int dimensionId, worldObjectId = -1;

	private IWorldObject worldObject;

	private NBTFunnel funnel;

	public PacketWorldObjectAdd()
	{
		super();
	}

	/**
	 * Packet part constructor.
	 * @param data The state.
	 */
	private PacketWorldObjectAdd(final byte[] data)
	{
		super(data);
	}

	public PacketWorldObjectAdd(final IWorldObject object, final int dimensionId)
	{
		this.worldObject = object;
		this.dimensionId = dimensionId;
	}

	public PacketWorldObjectAdd(final IWorldObject object, final int dimensionId, int worldObjectId)
	{
		this.worldObject = object;
		this.dimensionId = dimensionId;
		this.worldObjectId = worldObjectId;
	}

	public PacketWorldObjectAdd(final IWorldObject object)
	{
		this.worldObject = object;
		this.dimensionId = object.getWorld().provider.getDimension();
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);

		this.funnel = new NBTFunnel(tag);

		this.worldObjectId = buf.readInt();
		this.dimensionId = buf.readInt();
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("w", this.worldObject);

		ByteBufUtils.writeTag(buf, tag);

		buf.writeInt(this.worldObjectId);
		buf.writeInt(this.dimensionId);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketWorldObjectAdd(data);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketWorldObjectAdd, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketWorldObjectAdd message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final IWorldObject object = message.funnel.get(player.world, "w");

			final WorldObjectManager manager = WorldObjectManager.get(player.world);

			int id = manager.addObject(object);

			OrbisCore.network()
					.sendPacketToDimension(new PacketWorldObjectAdd(message.worldObject, message.dimensionId, id), message.dimensionId);

			return null;
		}
	}

	public static class HandlerClient extends MessageHandlerClient<PacketWorldObjectAdd, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketWorldObjectAdd message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			if (message.worldObjectId != -1)
			{
				final IWorldObject object = message.funnel.get(player.world, "w");

				final WorldObjectManager manager = WorldObjectManager.get(player.world);

				manager.setObject(message.worldObjectId, object);
			}

			return null;
		}
	}
}
