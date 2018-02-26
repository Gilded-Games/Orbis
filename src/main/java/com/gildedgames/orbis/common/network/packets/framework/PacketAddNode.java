package com.gildedgames.orbis.common.network.packets.framework;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.framework.FrameworkNode;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.MessageHandlerClient;
import com.gildedgames.orbis.common.network.MessageHandlerServer;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.world_objects.Framework;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class PacketAddNode implements IMessage
{

	private IDataIdentifier id;

	private int worldObjectId;

	private FrameworkNode node;

	private BlockPos pos;

	private NBTFunnel funnel;

	public PacketAddNode()
	{

	}

	public PacketAddNode(IDataIdentifier id, FrameworkNode node, final BlockPos pos)
	{
		this.id = id;
		this.node = node;
		this.pos = pos;
	}

	public PacketAddNode(Framework framework, FrameworkNode node, final BlockPos pos)
	{
		this.worldObjectId = WorldObjectManager.get(framework.getWorld()).getGroup(0).getID(framework);
		this.node = node;
		this.pos = pos;
	}

	public PacketAddNode(int worldObjectId, FrameworkNode node, final BlockPos pos)
	{
		this.worldObjectId = worldObjectId;
		this.node = node;
		this.pos = pos;
	}

	@Override
	public void fromBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = ByteBufUtils.readTag(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObjectId = tag.getInteger("worldObjectId");
		this.id = funnel.get("id");
		this.node = funnel.get("node");
		this.pos = funnel.getPos("p");
	}

	@Override
	public void toBytes(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("worldObjectId", this.worldObjectId);
		funnel.set("id", this.id);
		funnel.set("node", this.node);
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

				if (data instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data;

					fData.addNode(message.node, message.pos);
				}
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
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

				if (data instanceof FrameworkData)
				{
					final FrameworkData fData = (FrameworkData) data;

					fData.addNode(message.node, message.pos);

					// TODO: Send just to people who have downloaded this project
					// Should probably make it so IProjects track what players have
					// it downloaded on the client and up to date. That way we can
					// just send it to those players. Along with this, addNew a helper
					// method in NetworkingOrbis to sendPacketToProjectUsers
					if (player.world.getMinecraftServer().isDedicatedServer())
					{
						if (message.id == null)
						{
							NetworkingOrbis.sendPacketToAllPlayers(new PacketAddNode(message.worldObjectId, message.node, message.pos));
						}
						else
						{
							NetworkingOrbis.sendPacketToAllPlayers(new PacketAddNode(message.id, message.node, message.pos));
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
