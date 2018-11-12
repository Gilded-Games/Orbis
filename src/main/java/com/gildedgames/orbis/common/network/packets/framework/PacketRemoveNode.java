package com.gildedgames.orbis.common.network.packets.framework;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.framework.interfaces.IFrameworkNode;
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

public class PacketRemoveNode extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId, nodeId;

	public PacketRemoveNode()
	{

	}

	private PacketRemoveNode(final byte[] data)
	{
		super(data);
	}

	public PacketRemoveNode(IDataIdentifier id, int nodeId)
	{
		this.id = id;
		this.nodeId = nodeId;
	}

	public PacketRemoveNode(Framework framework, IFrameworkNode node)
	{
		this.worldObjectId = WorldObjectManager.get(framework.getWorld()).getID(framework);
		this.nodeId = framework.getData().getNodeId(node);
	}

	public PacketRemoveNode(int worldObjectId, int nodeId)
	{
		this.worldObjectId = worldObjectId;
		this.nodeId = nodeId;
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketRemoveNode(data);
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("w");
		this.id = funnel.get("i");
		this.nodeId = tag.getInteger("ni");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("w", this.worldObjectId);
		funnel.set("i", this.id);
		tag.setInteger("ni", this.nodeId);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketRemoveNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveNode message, final EntityPlayer player)
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
				if (data.get() instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data.get();

					fData.removeNode(message.nodeId);
				}
				else
				{
					OrbisCore.LOGGER.error("Data requested to be removed as a node is not actually a node", data.get());
				}

			}
			else if (message.id != null)
			{
				OrbisCore.LOGGER.error("Node requested to be removed doesn't exist in project", message.id);
			}
			else
			{
				OrbisCore.LOGGER.error("Node requested to be removed doesn't exist in the world", message.worldObjectId);
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketRemoveNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketRemoveNode message, final EntityPlayer player)
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
				if (data.get() instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data.get();

					boolean removed = fData.removeNode(message.nodeId);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (removed && player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketRemoveNode(message.worldObjectId, message.nodeId));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketRemoveNode(message.id, message.nodeId));
						}
					}
				}
				else
				{
					OrbisCore.LOGGER.error("Data requested to be removed as a node is not actually a node", data.get());
				}
			}
			else if (message.id != null)
			{
				OrbisCore.LOGGER.error("Node requested to be removed doesn't exist in project", message.id);
			}
			else
			{
				OrbisCore.LOGGER.error("Node requested to be removed doesn't exist in the world", message.worldObjectId);
			}

			return null;
		}
	}
}
