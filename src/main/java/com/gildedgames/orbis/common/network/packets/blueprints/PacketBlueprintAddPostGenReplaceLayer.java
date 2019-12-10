package com.gildedgames.orbis.common.network.packets.blueprints;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.data.schedules.PostGenReplaceLayer;
import com.gildedgames.orbis.lib.network.NetworkUtils;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerClient;
import com.gildedgames.orbis.lib.network.instances.MessageHandlerServer;
import com.gildedgames.orbis.lib.network.util.PacketMultipleParts;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.IWorldObject;
import com.gildedgames.orbis.lib.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketBlueprintAddPostGenReplaceLayer extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId, layerIndex;

	public PacketBlueprintAddPostGenReplaceLayer()
	{

	}

	private PacketBlueprintAddPostGenReplaceLayer(final byte[] data)
	{
		super(data);
	}

	public PacketBlueprintAddPostGenReplaceLayer(final IDataIdentifier id)
	{
		this.id = id;
		this.layerIndex = -1;
	}

	public PacketBlueprintAddPostGenReplaceLayer(final Blueprint blueprint)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.layerIndex = -1;
	}

	public PacketBlueprintAddPostGenReplaceLayer(final IDataIdentifier id, final int layerIndex)
	{
		this.id = id;
		this.layerIndex = layerIndex;
	}

	public PacketBlueprintAddPostGenReplaceLayer(final Blueprint blueprint, final int layerIndex)
	{
		this.worldObjectId = WorldObjectManager.get(blueprint.getWorld()).getID(blueprint);
		this.layerIndex = layerIndex;
	}

	public PacketBlueprintAddPostGenReplaceLayer(int worldObjectId, final int layerIndex)
	{
		this.worldObjectId = worldObjectId;
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
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("id", this.id);
		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setInteger("layerIndex", this.layerIndex);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketBlueprintAddPostGenReplaceLayer(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketBlueprintAddPostGenReplaceLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintAddPostGenReplaceLayer message, final EntityPlayer player)
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

			if (data.isPresent() && data.get() instanceof BlueprintData)
			{
				if (data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					bData.setPostGenReplaceLayer(message.layerIndex, new PostGenReplaceLayer(ItemStack.EMPTY, ItemStack.EMPTY));
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

	public static class HandlerServer extends MessageHandlerServer<PacketBlueprintAddPostGenReplaceLayer, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketBlueprintAddPostGenReplaceLayer message, final EntityPlayer player)
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

			if (data.isPresent() && data.get() instanceof BlueprintData)
			{
				if (data.get() instanceof BlueprintData)
				{
					final BlueprintData bData = (BlueprintData) data.get();

					int id = bData.addPostGenReplaceLayer(new PostGenReplaceLayer(ItemStack.EMPTY, ItemStack.EMPTY));

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketBlueprintAddPostGenReplaceLayer(message.worldObjectId, id));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketBlueprintAddPostGenReplaceLayer(message.id, id));
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
