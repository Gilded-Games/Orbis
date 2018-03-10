package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
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

		manager.addObject(this.worldObject);

		if (world.getMinecraftServer().isDedicatedServer())
		{
			OrbisAPI.network().sendPacketToDimension(new PacketWorldObjectAdd(this.worldObject), world.provider.getDimension());
		}
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (world.isRemote)
		{
			return;
		}

		final WorldObjectManager manager = WorldObjectManager.get(world);

		manager.removeObject(this.worldObject);

		if (world.getMinecraftServer().isDedicatedServer())
		{
			OrbisAPI.network().sendPacketToDimension(new PacketWorldObjectRemove(world, this.worldObject), world.provider.getDimension());
		}
	}

	@Override
	public void setWorld(World world)
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
