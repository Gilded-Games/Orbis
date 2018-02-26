package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.data.schedules.ISchedule;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.MessageHandlerClient;
import com.gildedgames.orbis.common.network.MessageHandlerServer;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketAddSchedule implements IMessage
{

	private IDataIdentifier id;

	private int worldObjectId;

	private ISchedule schedule;

	private int scheduleId = -1;

	private NBTFunnel funnel;

	public PacketAddSchedule()
	{

	}

	public PacketAddSchedule(IDataIdentifier id, ISchedule schedule, int scheduleId)
	{
		this.id = id;
		this.schedule = schedule;
		this.scheduleId = scheduleId;
	}

	public PacketAddSchedule(Blueprint blueprint, ISchedule schedule)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getGroup(0).getID(blueprint);
		this.schedule = schedule;
	}

	public PacketAddSchedule(Blueprint blueprint, ISchedule schedule, int scheduleId)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getGroup(0).getID(blueprint);
		this.schedule = schedule;
		this.scheduleId = scheduleId;
	}

	public PacketAddSchedule(int worldObjectId, ISchedule schedule, int scheduleId)
	{
		this.worldObjectId = worldObjectId;
		this.schedule = schedule;
		this.scheduleId = scheduleId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.id = funnel.get("id");
		this.schedule = funnel.get("schedule");
		this.scheduleId = tag.getInteger("scheduleId");
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("worldObjectId", this.worldObjectId);
		funnel.set("id", this.id);
		funnel.set("schedule", this.schedule);
		tag.setInteger("scheduleId", this.scheduleId);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketAddSchedule, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddSchedule message, final EntityPlayer player)
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

					if (message.scheduleId == -1)
					{
						bData.addSchedule(message.schedule);
					}
					else
					{
						bData.setSchedule(message.scheduleId, message.schedule);
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

	public static class HandlerServer extends MessageHandlerServer<PacketAddSchedule, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddSchedule message, final EntityPlayer player)
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

					int scheduleId = bData.addSchedule(message.schedule);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							NetworkingOrbis.sendPacketToAllPlayers(new PacketAddSchedule(message.worldObjectId, message.schedule, scheduleId));
						}
						else
						{
							NetworkingOrbis.sendPacketToAllPlayers(new PacketAddSchedule(message.id, message.schedule, scheduleId));
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
