package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.core.tree.NodeMultiParented;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.schedules.ScheduleLayer;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintAddScheduleLayer extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId, layerIndex;

	private String displayName;

	public PacketBlueprintAddScheduleLayer()
	{

	}

	private PacketBlueprintAddScheduleLayer(final byte[] data)
	{
		super(data);
	}

	public PacketBlueprintAddScheduleLayer(final IDataIdentifier id, final String displayName)
	{
		this.id = id;
		this.displayName = displayName;
		this.layerIndex = -1;
	}

	public PacketBlueprintAddScheduleLayer(final Blueprint blueprint, final String displayName)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.displayName = displayName;
		this.layerIndex = -1;
	}

	public PacketBlueprintAddScheduleLayer(final IDataIdentifier id, final String displayName, final int layerIndex)
	{
		this.id = id;
		this.displayName = displayName;
		this.layerIndex = layerIndex;
	}

	public PacketBlueprintAddScheduleLayer(final Blueprint blueprint, final String displayName, final int layerIndex)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.displayName = displayName;
		this.layerIndex = layerIndex;
	}

	public PacketBlueprintAddScheduleLayer(int worldObjectId, final String displayName, final int layerIndex)
	{
		this.worldObjectId = worldObjectId;
		this.displayName = displayName;
		this.layerIndex = layerIndex;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.id = funnel.get("id");
		this.worldObjectId = tag.getInteger("worldObjectId");
		this.layerIndex = tag.getInteger("layerIndex");
		this.displayName = tag.getString("getDisplayName");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.id);
		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("layerIndex", this.layerIndex);
		tag.setString("getDisplayName", this.displayName);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketBlueprintAddScheduleLayer(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketBlueprintAddScheduleLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintAddScheduleLayer message, final EntityPlayer player)
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
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

					data = worldObject.getData();
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data;

					bData.getScheduleLayerTree().put(message.layerIndex, new NodeMultiParented<>(new ScheduleLayer(message.displayName, bData), false));
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintAddScheduleLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintAddScheduleLayer message, final EntityPlayer player)
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
					final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

					data = worldObject.getData();
				}
				else
				{
					data = OrbisCore.getProjectManager().findData(message.id);
				}

				if (data instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data;

					int id = bData.getScheduleLayerTree().add(new NodeMultiParented<>(new ScheduleLayer(message.displayName, bData), false));

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketBlueprintAddScheduleLayer(message.worldObjectId, message.displayName, id));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketBlueprintAddScheduleLayer(message.id, message.displayName, id));
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
