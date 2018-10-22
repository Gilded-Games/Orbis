package com.gildedgames.orbis.common.network.packets.world_actions;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketApplyWorldAction extends PacketMultipleParts
{
	private IWorldAction action;

	private String worldActionLogId;

	private NBTFunnel funnel;

	public PacketApplyWorldAction()
	{

	}

	private PacketApplyWorldAction(byte[] data)
	{
		super(data);
	}

	public PacketApplyWorldAction(String worldActionLogId, IWorldAction action)
	{
		this.worldActionLogId = worldActionLogId;
		this.action = action;
	}

	@Override
	public PacketMultipleParts createPart(byte[] data)
	{
		return new PacketApplyWorldAction(data);
	}

	@Override
	public void read(final ByteBuf buf)
	{
		this.funnel = new NBTFunnel(NetworkUtils.readTagLimitless(buf));
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("i", this.worldActionLogId);
		funnel.set("a", this.action);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketApplyWorldAction, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketApplyWorldAction message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getWorldActionLog(message.funnel.getTag().getString("i")).apply(player.getEntityWorld(), message.funnel.get("a"));
			}

			return null;
		}
	}
}
