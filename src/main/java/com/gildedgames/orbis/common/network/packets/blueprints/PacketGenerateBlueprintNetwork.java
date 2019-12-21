package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketGenerateBlueprintNetwork implements IMessage
{

	private IDataIdentifier id;

	private BlockPos pos;

	public PacketGenerateBlueprintNetwork()
	{

	}

	public PacketGenerateBlueprintNetwork(IDataIdentifier id, BlockPos pos)
	{
		this.id = id;
		this.pos = pos;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.id = funnel.get("id");
		this.pos = funnel.getPos("pos");
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.id);
		funnel.setPos("pos", this.pos);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketGenerateBlueprintNetwork, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketGenerateBlueprintNetwork message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis != null)
			{
				playerOrbis.blueprintNetworks().addNewNetwork(message.id, message.pos);
			}

			return null;
		}
	}
}
