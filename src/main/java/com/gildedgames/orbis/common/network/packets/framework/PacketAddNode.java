package com.gildedgames.orbis.common.network.packets.framework;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.data.framework.FrameworkData;
import com.gildedgames.orbis_api.data.framework.FrameworkNode;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.Optional;

public class PacketAddNode extends PacketMultipleParts
{

	private IDataIdentifier id;

	private int worldObjectId;

	private FrameworkNode node;

	private BlockPos pos;

	public PacketAddNode()
	{

	}

	private PacketAddNode(final byte[] data)
	{
		super(data);
	}

	public PacketAddNode(IDataIdentifier id, FrameworkNode node, BlockPos pos)
	{
		this.id = id;
		this.node = node;
		this.pos = pos;
	}

	public PacketAddNode(Framework framework, FrameworkNode node, BlockPos pos)
	{
		this.worldObjectId = WorldObjectManager.get(framework.getWorld()).getID(framework);
		this.node = node;
		this.pos = pos;
	}

	public PacketAddNode(int worldObjectId, FrameworkNode node, BlockPos pos)
	{
		this.worldObjectId = worldObjectId;
		this.node = node;
		this.pos = pos;
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketAddNode(data);
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("w");
		this.id = funnel.get("i");
		this.node = funnel.get("n");
		this.pos = funnel.getPos("p");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("w", this.worldObjectId);
		funnel.set("i", this.id);
		funnel.set("n", this.node);
		funnel.setPos("p", this.pos);

		ByteBufUtils.writeTag(buf, tag);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketAddNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddNode message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			IWorldObject worldObject;

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
				if (data.get() instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data.get();

					fData.addNode(message.node, worldObject);
				}
				else
				{
					OrbisCore.LOGGER.error("Found data is not FrameworkData", data.get(), this.getClass());
				}
			}
			else if (message.id != null)
			{
				OrbisCore.LOGGER.error("Could not find data", message.id, this.getClass());
			}
			else
			{
				OrbisCore.LOGGER.error("Could not find data in world", message.worldObjectId, message.pos, this.getClass());
			}

			return null;
		}
	}

	public static class HandlerServer extends MessageHandlerServer<PacketAddNode, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketAddNode message, final EntityPlayer player)
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
				if (data.get() instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data.get();

					fData.addNode(message.node, worldObject);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketAddNode(message.worldObjectId, message.node, message.pos));
						}
						else
						{
							OrbisCore.network().sendPacketToAllPlayers(new PacketAddNode(message.id, message.node, message.pos));
						}
					}
				}
				else
				{
					OrbisCore.LOGGER.error("Found data is not FrameworkData", data.get(), this.getClass());
				}
			}
			else if (message.id != null)
			{
				OrbisCore.LOGGER.error("Could not find data", message.id, this.getClass());
			}
			else
			{
				OrbisCore.LOGGER.error("Could not find data in world", message.worldObjectId, message.pos, this.getClass());
			}

			return null;
		}
	}
}
