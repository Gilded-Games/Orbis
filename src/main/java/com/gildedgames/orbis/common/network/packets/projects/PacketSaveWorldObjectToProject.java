package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.management.IData;
import com.gildedgames.orbis_api.data.management.IProject;
import com.gildedgames.orbis_api.data.management.IProjectIdentifier;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerServer;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.io.File;
import java.util.Optional;

public class PacketSaveWorldObjectToProject extends PacketMultipleParts
{

	private IProjectIdentifier project;

	private int worldObjectId;

	private String location;

	public PacketSaveWorldObjectToProject()
	{

	}

	private PacketSaveWorldObjectToProject(final byte[] data)
	{
		super(data);
	}

	/**
	 *
	 * @param project The project that you want to save the object to.
	 * @param object The object we'll be fetching on the server and getting its state to save.
	 * @param location The location relative to the project directory.
	 */
	public PacketSaveWorldObjectToProject(final IProject project, final IWorldObject object, final String location)
	{
		this.project = project.getProjectIdentifier();
		this.worldObjectId = WorldObjectManager.get(object.getWorld()).getID(object);
		this.location = location;
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.project = funnel.get("project");
		this.worldObjectId = tag.getInteger("worldObjectId");
		this.location = tag.getString("location");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("project", this.project);
		tag.setInteger("worldObjectId", this.worldObjectId);
		tag.setString("location", this.location);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSaveWorldObjectToProject(data);
	}

	public static class HandlerServer extends MessageHandlerServer<PacketSaveWorldObjectToProject, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSaveWorldObjectToProject message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			try
			{
				final Optional<IProject> project = OrbisCore.getProjectManager().findProject(message.project);
				final IWorldObject worldObject = WorldObjectManager.get(player.world).getObject(message.worldObjectId);

				if (project.isPresent() && worldObject.getData() != null)
				{
					IData data = worldObject.getData();

					/**
					 * Check if the state has already been stored.
					 * If so, we should addNew a new identifier for it as
					 * a clone. Many issues are caused if two files use
					 * the same identifier.
					 */
					if (data.getMetadata().getIdentifier() != null && project.get().getCache().hasData(data.getMetadata().getIdentifier().getDataId()))
					{
						data = data.clone();
						data.getMetadata().setIdentifier(project.get().getCache().createNextIdentifier());
					}

					data.preSaveToDisk(worldObject);

					final File file = new File(project.get().getLocationAsFile(), message.location);

					project.get().getCache().setData(data, message.location);

					project.get().writeData(data, file);

					OrbisCore.network().sendPacketToPlayer(new PacketSendProjectListing(), (EntityPlayerMP) player);
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
