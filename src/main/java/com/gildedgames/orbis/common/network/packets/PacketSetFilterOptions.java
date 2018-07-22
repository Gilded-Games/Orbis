package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis_api.data.schedules.IFilterOptions;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketSetFilterOptions extends PacketMultipleParts
{

	private IFilterOptions filterOptions;

	public PacketSetFilterOptions()
	{

	}

	private PacketSetFilterOptions(final byte[] data)
	{
		super(data);
	}

	public PacketSetFilterOptions(IFilterOptions filterOptions)
	{
		this.filterOptions = filterOptions;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.filterOptions = funnel.get("f");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("f", this.filterOptions);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSetFilterOptions(data);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetFilterOptions, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetFilterOptions message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.powers().getFillPower().getFilterOptions().copyFrom(message.filterOptions);
			}

			return null;
		}
	}
}
