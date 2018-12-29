package com.gildedgames.orbis.common.network.packets.projects;

import com.gildedgames.orbis.client.gui.GuiSaveData;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.OrbisLib;
import com.gildedgames.orbis_api.data.management.*;
import com.gildedgames.orbis_api.network.NetworkUtils;
import com.gildedgames.orbis_api.network.instances.MessageHandlerClient;
import com.gildedgames.orbis_api.network.util.PacketMultipleParts;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class PacketSendProjectCache extends PacketMultipleParts
{

	private IProjectIdentifier project;

	private IProjectCache cache;

	private LocalDateTime lastChanged;

	public PacketSendProjectCache()
	{

	}

	private PacketSendProjectCache(final byte[] data)
	{
		super(data);
	}

	public PacketSendProjectCache(final IProject project)
	{
		this.project = project.getInfo().getIdentifier();
		this.cache = project.getCache();
		this.lastChanged = project.getInfo().getMetadata().getLastChanged();
	}

	@Override
	public void read(final ByteBuf buf)
	{
		final NBTTagCompound tag = NetworkUtils.readTagLimitless(buf);
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.project = funnel.get("project");
		this.cache = funnel.get("cache");
		this.lastChanged = funnel.getDate("lastChanged");
	}

	@Override
	public void write(final ByteBuf buf)
	{
		final NBTTagCompound tag = new NBTTagCompound();
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("project", this.project);
		funnel.set("cache", this.cache);
		funnel.setDate("lastChanged", this.lastChanged);

		ByteBufUtils.writeTag(buf, tag);
	}

	@Override
	public PacketMultipleParts createPart(final byte[] data)
	{
		return new PacketSendProjectCache(data);
	}

	public static class HandlerClient extends MessageHandlerClient<PacketSendProjectCache, IMessage>
	{
		@Override
		public IMessage onMessage(final PacketSendProjectCache message, final EntityPlayer player)
		{
			if (player == null || player.world == null)
			{
				return null;
			}

			final Optional<IProject> project = OrbisCore.getProjectManager().findProject(message.project);

			if (project.isPresent())
			{
				project.get().setCache(message.cache);

				/**
				 * Save all state to disk.
				 */
				for (final IData data : message.cache.getAllData())
				{
					Optional<String> dataLoc = message.cache.getDataLocation(data.getMetadata().getIdentifier().getDataId());

					if (dataLoc.isPresent())
					{
						final File file = new File(project.get().getLocationAsFile(), dataLoc.get());

						String extension = FilenameUtils.getExtension(dataLoc.get());

						Optional<IDataLoader<IProject>> dataLoader = OrbisLib.services().getProjectManager().getDataLoaderForExtension(extension);
						Optional<IMetadataLoader<IProject>> metadataLoader = OrbisLib.services().getProjectManager()
								.getMetadataLoaderForExtension(extension);

						if (!dataLoader.isPresent() || !metadataLoader.isPresent())
						{
							OrbisCore.LOGGER.error("Failed to save project data state (" + data.getMetadata().getIdentifier() + ") to disk");
							continue;
						}

						try (FileOutputStream stream = new FileOutputStream(file))
						{
							dataLoader.get().saveData(project.get(), data, file, stream);
						}
						catch (IOException e)
						{
							OrbisLib.LOGGER.error("Failed to write data to project directory", data, e);
						}
					}
				}

				project.get().getInfo().getMetadata().setDownloaded(true);
				project.get().getInfo().getMetadata().setDownloading(false);

				project.get().getInfo().getMetadata().setLastChanged(message.lastChanged);

				OrbisCore.LOGGER.debug("Project downloaded! " + project.get().getLocationAsFile().getName());
			}
			else
			{
				OrbisCore.LOGGER.error("Could not find project to send project cache to", message.project);
			}

			if (Minecraft.getMinecraft().currentScreen instanceof GuiSaveData)
			{
				final GuiSaveData viewProjects = (GuiSaveData) Minecraft.getMinecraft().currentScreen;

				viewProjects.refreshNavigator();
			}

			return null;
		}
	}
}
