package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.data.schedules.ISchedule;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.WorldObjectManager;
import com.gildedgames.orbis.lib.world.WorldObjectUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketAddSchedule implements IMessage
{

	private IDataIdentifier id;

	private int worldObjectId;

	private ISchedule schedule;

	private int scheduleId = -1, layerId = -1;

	private BlockPos pos;

	public PacketAddSchedule()
	{

	}

	public PacketAddSchedule(IDataIdentifier id, ISchedule schedule, int layerId, int scheduleId, BlockPos pos)
	{
		this.id = id;
		this.schedule = schedule;
		this.scheduleId = scheduleId;
		this.layerId = layerId;
		this.pos = pos;
	}

	public PacketAddSchedule(Blueprint blueprint, ISchedule schedule, int layerId)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.schedule = schedule;
		this.layerId = layerId;
		this.pos = blueprint.getPos();
	}

	public PacketAddSchedule(Blueprint blueprint, ISchedule schedule, int layerId, int scheduleId)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.layerId = layerId;
		this.schedule = schedule;
		this.scheduleId = scheduleId;
		this.pos = blueprint.getPos();
	}

	public PacketAddSchedule(int worldObjectId, ISchedule schedule, int layerId, int scheduleId, BlockPos pos)
	{
		this.worldObjectId = worldObjectId;
		this.schedule = schedule;
		this.scheduleId = scheduleId;
		this.layerId = layerId;
		this.pos = pos;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.id = funnel.get("id");
		this.schedule = funnel.get("schedule");
		this.scheduleId = tag.getInteger("scheduleId");
		this.layerId = tag.getInteger("layerId");
		this.pos = funnel.getPos("pos");
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
		tag.setInteger("layerId", this.layerId);
		funnel.setPos("pos", this.pos);

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

			final IWorldObject worldObject;

			if (message.worldObjectId == -1)
			{
				worldObject = WorldObjectUtils.getIntersectingShape(player.world, Blueprint.class, message.pos);
			}
			else
			{
				worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);
			}

			final Optional<IData> data;

			if (message.id == null)
			{
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

					if (message.scheduleId == -1)
					{
						bData.getScheduleLayerTree().get(message.layerId).getData().getScheduleRecord().addSchedule(message.schedule, worldObject);
					}
					else
					{
						bData.getScheduleLayerTree().get(message.layerId).getData().getScheduleRecord()
								.setSchedule(message.scheduleId, message.schedule, worldObject);
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

	public static class HandlerServer extends MessageHandlerServer<PacketAddSchedule, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddSchedule message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final IWorldObject worldObject;

			if (message.worldObjectId == -1)
			{
				worldObject = WorldObjectUtils.getIntersectingShape(player.world, Blueprint.class, message.pos);
			}
			else
			{
				worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);
			}

			final Optional<IData> data;

			if (message.id == null)
			{
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

					int scheduleId = bData.getScheduleLayerTree().get(message.layerId).getData().getScheduleRecord().addSchedule(message.schedule, worldObject);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network()
									.sendPacketToAllPlayers(
											new PacketAddSchedule(message.worldObjectId, message.schedule, message.layerId, scheduleId, message.pos));
						}
						else
						{
							OrbisCore.network()
									.sendPacketToAllPlayers(new PacketAddSchedule(message.id, message.schedule, message.layerId, scheduleId, message.pos));
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
