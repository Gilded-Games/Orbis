package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.pathway.Entrance;
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

public class PacketRemoveEntrance implements IMessage
{

	private IDataIdentifier id;

	private int worldObjectId;

	private int entranceId = -1;

	private NBTFunnel funnel;

	public PacketRemoveEntrance()
	{

	}

	public PacketRemoveEntrance(IDataIdentifier id, int entranceId)
	{
		this.id = id;
		this.entranceId = entranceId;
	}

	public PacketRemoveEntrance(IDataIdentifier id, Entrance entrance)
	{
		this.id = id;
		this.entranceId = entrance.getDataParent().getEntranceId(entrance);
	}

	public PacketRemoveEntrance(Blueprint blueprint, Entrance entrance)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.entranceId = blueprint.getData().getEntranceId(entrance);
	}

	public PacketRemoveEntrance(int worldObjectId, int scheduleId)
	{
		this.worldObjectId = worldObjectId;
		this.entranceId = scheduleId;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.id = funnel.get("id");
		this.entranceId = tag.getInteger("entranceId");
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("worldObjectId", this.worldObjectId);
		funnel.set("id", this.id);
		tag.setInteger("entranceId", this.entranceId);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketRemoveEntrance, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveEntrance message, final EntityPlayer player)
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

					bData.removeEntrance(message.entranceId);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketRemoveEntrance, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveEntrance message, final EntityPlayer player)
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

					bData.removeEntrance(message.entranceId);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketRemoveEntrance(message.worldObjectId, message.entranceId));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketRemoveEntrance(message.id, message.entranceId));
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
