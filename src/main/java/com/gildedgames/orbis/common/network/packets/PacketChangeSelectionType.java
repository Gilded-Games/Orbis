package com.gildedgames.orbis.common.network.packets;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.player.designer_mode.ISelectionType;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.UUID;

public class PacketChangeSelectionType implements IMessage
{
	private UUID selectionTypeIndex;

	public PacketChangeSelectionType()
	{

	}

	public PacketChangeSelectionType(final PlayerOrbis playerOrbis, final ISelectionType selectionType)
	{
		this.selectionTypeIndex = playerOrbis.selectionTypes().getUniqueId(selectionType);
	}

	public PacketChangeSelectionType(final UUID uniqueId)
	{
		this.selectionTypeIndex = uniqueId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		NBTTagCompound tag = ByteBufUtils.readTag(buf);

		if (tag != null)
		{
			this.selectionTypeIndex = tag.getUniqueId("uniqueId");
		}
		else
		{
			OrbisCore.LOGGER.info("Could not read back tag when changing selection type in packet.");
		}
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		NBTTagCompound tag = new NBTTagCompound();

		tag.setUniqueId("uniqueId", this.selectionTypeIndex);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketChangeSelectionType, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeSelectionType message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.selectionTypes().setCurrentSelectionType(message.selectionTypeIndex);
			}

			return null;
		}
	}

	public static class HandlerClient extends MessageHandlerClient<PacketChangeSelectionType, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketChangeSelectionType message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis.inDeveloperMode())
			{
				playerOrbis.selectionTypes().setCurrentSelectionType(message.selectionTypeIndex);
			}

			return null;
		}
	}
}
