package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.packets.instances.MessageHandlerClient;
import com.gildedgames.orbis.api.packets.instances.MessageHandlerServer;
import com.gildedgames.orbis.api.packets.util.PacketMultipleParts;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketBlueprintSetCurrentScheduleLayer extends PacketMultipleParts
{

	private int worldObjectId;

	private int scheduleLayerIndex;

	public PacketBlueprintSetCurrentScheduleLayer()
	{

	}

	private PacketBlueprintSetCurrentScheduleLayer(final byte[] data)
	{
		super(data);
	}

	public PacketBlueprintSetCurrentScheduleLayer(final Blueprint blueprint, final int scheduleLayerIndex)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getGroup(0).getID(blueprint);
		this.scheduleLayerIndex = scheduleLayerIndex;
	}

	public PacketBlueprintSetCurrentScheduleLayer(final int worldObjectId, final int scheduleLayerIndex)
	{
		this.worldObjectId = worldObjectId;
		this.scheduleLayerIndex = scheduleLayerIndex;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.scheduleLayerIndex = tag.getInteger("scheduleLayerIndex");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();

		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("scheduleLayerIndex", this.scheduleLayerIndex);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketBlueprintSetCurrentScheduleLayer(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketBlueprintSetCurrentScheduleLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintSetCurrentScheduleLayer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IWorldObject worldObject = WorldObjectManager.get(player.world).getGroup(0).getObject(message.worldObjectId);

				if (worldObject instanceof Blueprint)
				{
					final Blueprint b = (Blueprint) worldObject;

					b.setCurrentScheduleLayerIndex(message.scheduleLayerIndex);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintSetCurrentScheduleLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintSetCurrentScheduleLayer message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final IWorldObject worldObject = WorldObjectManager.get(player.world).getGroup(0).getObject(message.worldObjectId);

				if (worldObject instanceof Blueprint)
				{
					final Blueprint b = (Blueprint) worldObject;

					b.setCurrentScheduleLayerIndex(message.scheduleLayerIndex);

					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						OrbisAPI.network()
								.sendPacketToAllPlayers(new PacketBlueprintSetCurrentScheduleLayer(message.worldObjectId, message.scheduleLayerIndex));
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
