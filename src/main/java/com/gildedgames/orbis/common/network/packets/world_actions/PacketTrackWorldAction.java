package com.gildedgames.orbis.common.network.packets.world_actions;

import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketTrackWorldAction implements IMessage
{
	private IWorldAction action;

	public PacketTrackWorldAction()
	{

	}

	public PacketTrackWorldAction(IWorldAction action)
	{
		this.action = action;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		NBTFunnel funnel = new NBTFunnel(ByteBufUtils.readTag(buf));

		this.action = funnel.get("a");
	}

	@Override
	public void toBytes(final ByteBuf buf)
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
				playerOrbis.getWorldActionLog().track(player.getEntityWorld(), message.action);
			}

			return null;
		}
	}
}
