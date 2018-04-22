package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionAddWorldObject implements IWorldAction
{

	private IWorldObject worldObject;

	private WorldActionAddWorldObject()
	{

	}

	public WorldActionAddWorldObject(IWorldObject worldObject)
	{
		this.worldObject = worldObject;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (world.isRemote)
		{
			return;
		}

		final WorldObjectManager manager = WorldObjectManager.get(world);

		int id = manager.fetchNextId();

		if (world.getMinecraftServer().isDedicatedServer())
		{
			manager.setObject(id, this.worldObject);
		}

		OrbisCore.network()
				.sendPacketToDimension(new PacketWorldObjectAdd(this.worldObject, world.provider.getDimension(), id),
						world.provider.getDimension());
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (world.isRemote)
		{
			return;
		}

		final WorldObjectManager manager = WorldObjectManager.get(world);

		if (world.getMinecraftServer().isDedicatedServer())
		{
			manager.removeObject(this.worldObject);
		}

		OrbisCore.network().sendPacketToDimension(new PacketWorldObjectRemove(world, this.worldObject), world.provider.getDimension());
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{
		this.worldObject.setWorld(world);
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("w", this.worldObject);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.worldObject = funnel.get("w");
	}
}
