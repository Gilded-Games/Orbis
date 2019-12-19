package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketSetEntranceTriggerId implements IMessage
{

	private IDataIdentifier id;

	private int worldObjectId;

	private String triggerId;

	public PacketSetEntranceTriggerId()
	{

	}

	public PacketSetEntranceTriggerId(IDataIdentifier id, String triggerId)
	{
		this.id = id;
		this.triggerId = triggerId;
	}

	public PacketSetEntranceTriggerId(Blueprint blueprint, String triggerId)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.triggerId = triggerId;
	}

	public PacketSetEntranceTriggerId(int worldObjectId, String triggerId)
	{
		this.worldObjectId = worldObjectId;

		this.triggerId = triggerId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.id = funnel.get("id");

		this.triggerId = tag.getString("triggerId");
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("worldObjectId", this.worldObjectId);
		funnel.set("id", this.id);

		tag.setString("triggerId", this.triggerId);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSetEntranceTriggerId, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetEntranceTriggerId message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IData> data;

			if (message.id == null)
			{
				final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

				data = Optional.of(worldObject.getData());
			}
			else
			{
				data = OrbisCore.getProjectManager().findData(message.id);
			}

			if (data.isPresent())
			{
				if (data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					bData.getEntrance().setTriggerId(message.triggerId);
				}
				else
				{
					OrbisCore.LOGGER.error("Data isn't a blueprint", message.id, this.getClass());
				}
			}
			else if (message.id != null)
			{
				OrbisCore.LOGGER.error("Blueprint doesn't exist in project", message.id, this.getClass());
			}
			else
			{
				OrbisCore.LOGGER.error("Blueprint doesn't exist in the world", message.worldObjectId, this.getClass());
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSetEntranceTriggerId, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSetEntranceTriggerId message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IData> data;

			if (message.id == null)
			{
				final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

				data = Optional.of(worldObject.getData());
			}
			else
			{
				data = OrbisCore.getProjectManager().findData(message.id);
			}

			if (data.isPresent())
			{
				if (data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					bData.getEntrance().setTriggerId(message.triggerId);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(
									new PacketSetEntranceTriggerId(message.worldObjectId, message.triggerId));
						}
						else
						{
							OrbisCore.network()
									.sendPacketToAllPlayers(new PacketSetEntranceTriggerId(message.id, message.triggerId));
						}
					}
				}
				else
				{
					OrbisCore.LOGGER.error("Data isn't a blueprint", message.id, this.getClass());
				}
			}
			else if (message.id != null)
			{
				OrbisCore.LOGGER.error("Blueprint doesn't exist in project", message.id, this.getClass());
			}
			else
			{
				OrbisCore.LOGGER.error("Blueprint doesn't exist in the world", message.worldObjectId, this.getClass());
			}

			return null;
		}
	}
}
