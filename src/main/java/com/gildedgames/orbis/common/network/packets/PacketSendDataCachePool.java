package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.lib.data.management.IDataCachePool;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.util.PacketMultipleParts;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSendDataCachePool extends PacketMultipleParts
{

	private IDataCachePool pool;

	private NBTTagCompound cacheData;

	public PacketSendDataCachePool()
	{

	}

	private PacketSendDataCachePool(final byte[] data)
	{
		super(data);
	}

	public PacketSendDataCachePool(final IDataCachePool pool)
	{
		this.pool = pool;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		this.cacheData = NetworkUtils.readTagLimitless(buf);
	}

	@Override
	public void write(final ByteBuf buf)
	{
		ByteBufUtils.writeTag(buf, this.pool.writeCacheData());
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSendDataCachePool(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSendDataCachePool, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSendDataCachePool message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			OrbisCore.getDataCache().readCacheData(message.cacheData);
			OrbisCore.getDataCache().flushToDisk();

			return null;
		}
	}
}
