package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.MessageHandlerClient;
import com.gildedgames.orbis.common.network.MessageHandlerServer;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.util.PacketMultipleParts;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintRemoveScheduleLayer extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId;

	private int scheduleLayerIndex;

	public PacketBlueprintRemoveScheduleLayer()
	{

	}

	private PacketBlueprintRemoveScheduleLayer(final byte[] data)
	{
		super(data);
	}

	public PacketBlueprintRemoveScheduleLayer(final IDataIdentifier id, final int scheduleLayerIndex)
	{
		this.id = id;
		this.scheduleLayerIndex = scheduleLayerIndex;
	}

	public PacketBlueprintRemoveScheduleLayer(final Blueprint blueprint, final int scheduleLayerIndex)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getGroup(0).getID(blueprint);
		this.scheduleLayerIndex = scheduleLayerIndex;
	}

	public PacketBlueprintRemoveScheduleLayer(final int worldObjectId, final int scheduleLayerIndex)
	{
		this.worldObjectId = worldObjectId;
		this.scheduleLayerIndex = scheduleLayerIndex;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.id = funnel.get("id");
		this.worldObjectId = tag.getInteger("worldObjectId");
		this.scheduleLayerIndex = tag.getInteger("scheduleLayerIndex");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.id);
		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("scheduleLayerIndex", this.scheduleLayerIndex);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketBlueprintRemoveScheduleLayer(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketBlueprintRemoveScheduleLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintRemoveScheduleLayer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IData data;

				if (message.id == null)
				{
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getGroup(0).getObject(message.worldObjectId);

					data = worldObject.getData();
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data;

					bData.removeScheduleLayer(message.scheduleLayerIndex);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintRemoveScheduleLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintRemoveScheduleLayer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IData data;

				if (message.id == null)
				{
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getGroup(0).getObject(message.worldObjectId);

					data = worldObject.getData();
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data;

					bData.removeScheduleLayer(message.scheduleLayerIndex);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							NetworkingOrbis.sendPacketToAllPlayers(new PacketBlueprintRemoveScheduleLayer(message.worldObjectId, message.scheduleLayerIndex));
						}
						else
						{
							NetworkingOrbis.sendPacketToAllPlayers(new PacketBlueprintRemoveScheduleLayer(message.id, message.scheduleLayerIndex));
						}
					}
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}
}
