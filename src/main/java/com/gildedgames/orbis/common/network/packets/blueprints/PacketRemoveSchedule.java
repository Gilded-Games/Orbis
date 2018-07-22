package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.schedules.ISchedule;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketRemoveSchedule implements IMessage
{

	private IDataIdentifier id;

	private int worldObjectId;

	private int scheduleId = -1, layerId = -1;

	private NBTFunnel funnel;

	public PacketRemoveSchedule()
	{

	}

	public PacketRemoveSchedule(IDataIdentifier id, int layerId, int scheduleId)
	{
		this.id = id;
		this.scheduleId = scheduleId;
		this.layerId = layerId;
	}

	public PacketRemoveSchedule(Blueprint blueprint, ISchedule schedule)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.scheduleId = schedule.getParent().getScheduleId(schedule);
		this.layerId = schedule.getParent().getParent().getNodeParent().getNodeId();
	}

	public PacketRemoveSchedule(int worldObjectId, int layerId, int scheduleId)
	{
		this.worldObjectId = worldObjectId;
		this.scheduleId = scheduleId;
		this.layerId = layerId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.id = funnel.get("id");
		this.scheduleId = tag.getInteger("scheduleId");
		this.layerId = tag.getInteger("layerId");
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("worldObjectId", this.worldObjectId);
		funnel.set("id", this.id);
		tag.setInteger("scheduleId", this.scheduleId);
		tag.setInteger("layerId", this.layerId);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketRemoveSchedule, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveSchedule message, final EntityPlayer player)
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

					bData.getScheduleLayerTree().get(message.layerId).getData().getScheduleRecord().removeSchedule(message.scheduleId);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketRemoveSchedule, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveSchedule message, final EntityPlayer player)
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

					bData.getScheduleLayerTree().get(message.layerId).getData().getScheduleRecord().removeSchedule(message.scheduleId);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketRemoveSchedule(message.worldObjectId, message.layerId, message.scheduleId));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketRemoveSchedule(message.id, message.layerId, message.scheduleId));
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
