package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.client.rect.Pos2D;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
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

import java.util.Optional;

public class PacketBlueprintScheduleLayerGuiPos extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId;

	private int scheduleLayerIndex;

	private Pos2D guiPos;

	public PacketBlueprintScheduleLayerGuiPos()
	{

	}

	private PacketBlueprintScheduleLayerGuiPos(final byte[] data)
	{
		super(data);
	}

	public PacketBlueprintScheduleLayerGuiPos(final IDataIdentifier id, final int scheduleLayerIndex, Pos2D guiPos)
	{
		this.id = id;
		this.scheduleLayerIndex = scheduleLayerIndex;
		this.guiPos = guiPos;
	}

	public PacketBlueprintScheduleLayerGuiPos(final Blueprint blueprint, final int scheduleLayerIndex, Pos2D guiPos)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.scheduleLayerIndex = scheduleLayerIndex;
		this.guiPos = guiPos;
	}

	public PacketBlueprintScheduleLayerGuiPos(final int worldObjectId, final int scheduleLayerIndex, Pos2D guiPos)
	{
		this.worldObjectId = worldObjectId;
		this.scheduleLayerIndex = scheduleLayerIndex;
		this.guiPos = guiPos;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.id = funnel.get("id");
		this.worldObjectId = tag.getInteger("worldObjectId");
		this.scheduleLayerIndex = tag.getInteger("scheduleLayerIndex");
		this.guiPos = funnel.get("guiPos", NBTFunnel.POS2D_GETTER);
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.id);
		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("scheduleLayerIndex", this.scheduleLayerIndex);
		funnel.set("guiPos", this.guiPos, NBTFunnel.POS2D_SETTER);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketBlueprintScheduleLayerGuiPos(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketBlueprintScheduleLayerGuiPos, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintScheduleLayerGuiPos message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
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

				if (data.isPresent() && data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					bData.getScheduleLayerTree().get(message.scheduleLayerIndex).getData().setGuiPos(message.guiPos);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintScheduleLayerGuiPos, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintScheduleLayerGuiPos message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
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

				if (data.isPresent() && data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					bData.getScheduleLayerTree().get(message.scheduleLayerIndex).getData().setGuiPos(message.guiPos);

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
											new PacketBlueprintScheduleLayerGuiPos(message.worldObjectId, message.scheduleLayerIndex, message.guiPos));
						}
						else
						{
							OrbisCore.network()
									.sendPacketToAllPlayers(new PacketBlueprintScheduleLayerGuiPos(message.id, message.scheduleLayerIndex, message.guiPos));
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
