package com.gildedgames.orbis.api.world.instances;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.packets.instances.PacketRegisterDimension;
import com.gildedgames.orbis.api.packets.instances.PacketUnregisterDimension;
import com.gildedgames.orbis.api.util.TeleporterGeneric;
import com.gildedgames.orbis.api.util.mc.BlockPosDimension;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class InstanceHandler<T extends IInstance> implements IInstanceHandler<T>
{

	private final BiMap<Integer, T> instances = HashBiMap.create();

	private final IInstanceFactory<T> factory;

	public InstanceHandler(final IInstanceFactory<T> factory)
	{
		this.factory = factory;
	}

	@Override
	public T getInstance(final int id)
	{
		return this.instances.get(id);
	}

	@Override
	public T createNew()
	{
		final int dimensionId = this.getFreeDimID();

		DimensionManager.registerDimension(dimensionId, this.factory.dimensionType());

		if (OrbisAPI.isServer())
		{
			OrbisAPI.network().sendPacketToAllPlayers(new PacketRegisterDimension(this.factory.dimensionType(), dimensionId));
		}

		final T instance = this.factory.createInstance(dimensionId, this);
		this.instances.put(dimensionId, instance);

		return instance;
	}

	@Override
	public void sendUnregisterInstancesPacket(final EntityPlayerMP player)
	{
		if (!OrbisAPI.isServer())
		{
			return;
		}

		for (final Map.Entry<Integer, T> entry : this.instances.entrySet())
		{
			final int dimId = entry.getKey();

			OrbisAPI.network().sendPacketToPlayer(new PacketUnregisterDimension(dimId), player);
		}
	}

	@Override
	public void unregisterInstances()
	{
		for (final Map.Entry<Integer, T> entry : this.instances.entrySet())
		{
			final int dimId = entry.getKey();

			DimensionManager.unregisterDimension(dimId);
		}

		this.instances.clear();
	}

	@Override
	public void registerInstance(T instance, int id)
	{
		if (DimensionManager.isDimensionRegistered(id))
		{
			final int oldId = id;

			final File oldDimFolder = new File(OrbisAPI.getWorldDirectory(), "//DIM" + oldId);

			id = this.getFreeDimID();

			final File newDimFolder = new File(OrbisAPI.getWorldDirectory(), "//DIM" + id);

			if (oldDimFolder.isDirectory())
			{
				final File[] content = oldDimFolder.listFiles();

				for (int i = 0; i < content.length; i++)
				{
					try
					{
						FileUtils.moveToDirectory(content[i], newDimFolder, true);
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			instance.setDimensionId(id);
		}

		DimensionManager.registerDimension(id, this.factory.dimensionType());

		this.instances.put(id, instance);
	}

	@Override
	public void write(final NBTTagCompound output)
	{
		output.setBoolean("hasWrittenInstances", this.instances.size() > 0);

		final NBTTagList tagList = new NBTTagList();

		for (final Map.Entry<Integer, T> entry : this.instances.entrySet())
		{
			final T instance = entry.getValue();
			final NBTTagCompound newTag = new NBTTagCompound();
			newTag.setInteger("dimension", entry.getKey());

			instance.write(newTag);
			tagList.appendTag(newTag);
		}

		output.setTag("instances", tagList);
	}

	@Override
	public void read(final NBTTagCompound input)
	{
		final boolean hasWrittenInstances = input.getBoolean("hasWrittenInstances");

		if (!hasWrittenInstances)
		{
			return;
		}

		for (final NBTTagCompound tag : NBTHelper.getIterator(input, "instances"))
		{
			int id = tag.getInteger("dimension");

			final T instance = this.factory.createInstance(id, this);
			instance.read(tag);

			this.registerInstance(instance, id);
		}
	}

	@Override
	public T getInstanceForDimension(final int id)
	{
		return this.instances.get(id);
	}

	@Override
	public int getDimensionForInstance(final IInstance instance)
	{
		return this.instances.inverse().get(instance);
	}

	@Override
	public World getWorldForInstance(final IInstance instance)
	{
		final int dim = this.getDimensionForInstance(instance);

		final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		return server.getWorld(dim);
	}

	@Override
	public int getInstancesSize()
	{
		return this.instances.size();
	}

	@Override
	public Collection<T> getInstances()
	{
		return this.instances.values();
	}

	@Override
	public BiMap<Integer, T> getInstancesMap()
	{
		return this.instances;
	}

	@Override
	public World teleportPlayerToDimension(final T instance, final EntityPlayerMP player)
	{
		OrbisAPI.network().sendPacketToPlayer(new PacketRegisterDimension(instance.getDimensionType(), instance.getDimensionId()), player);

		if (this.instances.containsValue(instance))
		{
			final IPlayerInstances hook = OrbisAPI.instances().getPlayer(player);

			if (hook.getInstance() == null)
			{
				hook.setOutside(new BlockPosDimension((int) player.posX, (int) player.posY, (int) player.posZ, player.dimension));
			}

			final int dimId = this.instances.inverse().get(instance);

			final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

			final WorldServer world = server.getWorld(dimId);

			final Teleporter teleporter = this.factory.getTeleporter(world);

			final PlayerList playerList = server.getPlayerList();
			playerList.transferPlayerToDimension(player, this.instances.inverse().get(instance), teleporter);

			player.timeUntilPortal = player.getPortalCooldown();

			hook.setInstance(instance);

			return world;
		}

		return player.world;
	}

	@Override
	public void teleportPlayerOutsideInstance(final EntityPlayerMP player)
	{
		final IPlayerInstances hook = OrbisAPI.instances().getPlayer(player);

		if (hook.getInstance() != null && hook.outside() != null)
		{
			final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

			final BlockPosDimension pos = hook.outside();
			final Teleporter teleporter = new TeleporterGeneric(server.getWorld(player.dimension));
			final PlayerList playerList = server.getPlayerList();
			playerList.transferPlayerToDimension(player, pos.getDim(), teleporter);
			player.timeUntilPortal = player.getPortalCooldown();
			hook.setOutside(null);
			hook.setInstance(null);

			player.connection.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), 0, 0);
		}
	}

	public int getFreeDimID()
	{
		int next = -1;

		while (true)
		{
			next--;

			if (!DimensionManager.isDimensionRegistered(next))
			{
				return next;
			}
		}
	}

}
