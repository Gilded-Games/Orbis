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

public class PacketTrackWorldAction extends PacketMultipleParts
{
	private IWorldAction action;

	private NBTFunnel funnel;

	public PacketTrackWorldAction()
	{

	}

	private PacketTrackWorldAction(byte[] data)
	{
		super(data);
	}

	public PacketTrackWorldAction(IWorldAction action)
	{
		this.action = action;
	}

	@Override
	public PacketMultipleParts createPart(byte[] data)
	{
		return new PacketTrackWorldAction(data);
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

		funnel.set("a", this.action);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketTrackWorldAction, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketTrackWorldAction message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.getWorldActionLog().track(player.getEntityWorld(), message.funnel.get("a"));
			}

			return null;
		}
	}
}
