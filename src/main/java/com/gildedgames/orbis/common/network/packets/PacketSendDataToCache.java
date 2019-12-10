package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataCache;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.util.PacketMultipleParts;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketSendDataToCache extends PacketMultipleParts
{

	private String cacheId;

	private IData data;

	public PacketSendDataToCache()
	{

	}

	private PacketSendDataToCache(final byte[] data)
	{
		super(data);
	}

	public PacketSendDataToCache(final String cacheId, final IData data)
	{
		this.cacheId = cacheId;
		this.data = data;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.cacheId = tag.getString("cacheId");
		this.data = funnel.get("state");

		// Set the metadata back in when reading everything
		this.data.setMetadata(funnel.get("meta"));
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("cacheId", this.cacheId);
		funnel.set("state", this.data);

		// Metadata is not serialized by default in IData, so we serialise manually
		funnel.set("meta", this.data.getMetadata());

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSendDataToCache(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSendDataToCache, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSendDataToCache message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IDataCache> cache = OrbisCore.getDataCache().findCache(message.cacheId);

			cache.ifPresent(iDataCache -> iDataCache.setData(message.data.getMetadata().getIdentifier().getDataId(), message.data));

			return null;
		}
	}
}
