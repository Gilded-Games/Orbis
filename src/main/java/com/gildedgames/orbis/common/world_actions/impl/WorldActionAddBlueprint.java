package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.BlueprintHelper;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionAddBlueprint implements IWorldAction
{

	private Blueprint blueprint;

	private BlockDataContainer oldContent;

	private WorldActionAddBlueprint()
	{

	}

	public WorldActionAddBlueprint(Blueprint blueprint)
	{
		this.blueprint = blueprint;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (world.isRemote)
		{
			return;
		}

		this.oldContent = BlueprintHelper.fetchBlocksInside(this.blueprint, world);

		final WorldObjectManager manager = WorldObjectManager.get(world);

		manager.addObject(this.blueprint);

		if (world.getMinecraftServer().isDedicatedServer())
		{
			OrbisAPI.network().sendPacketToDimension(new PacketWorldObjectAdd(this.blueprint), world.provider.getDimension());
		}

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.blueprint.getBlockDataContainer(), new CreationData(world).pos(this.blueprint.getMin()));
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (world.isRemote)
		{
			return;
		}

		final WorldObjectManager manager = WorldObjectManager.get(world);

		manager.removeObject(this.blueprint);

		if (world.getMinecraftServer().isDedicatedServer())
		{
			OrbisAPI.network().sendPacketToDimension(new PacketWorldObjectRemove(world, this.blueprint), world.provider.getDimension());
		}

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.oldContent, new CreationData(world).pos(this.blueprint.getMin()));
	}

	@Override
	public void setWorld(World world)
	{
		this.blueprint.setWorld(world);
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("w", this.blueprint);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.blueprint = funnel.get("w");
	}
}
