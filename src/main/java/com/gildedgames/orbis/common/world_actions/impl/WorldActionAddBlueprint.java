package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.BlueprintHelper;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectAdd;
import com.gildedgames.orbis.common.network.packets.PacketWorldObjectRemove;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldActionAddBlueprint implements IWorldAction
{

	private BlockPos createPos;

	private Blueprint blueprint;

	private BlockDataContainer oldContent;

	public WorldActionAddBlueprint(BlockPos createPos)
	{
		this.createPos = createPos;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (world.isRemote)
		{
			return;
		}

		if (this.blueprint == null)
		{
			Rotation rotation = player.powers().getBlueprintPower().getPlacingRotation();
			BlueprintData data = player.powers().getBlueprintPower().getPlacingBlueprint();

			IRegion bb = RotationHelp.regionFromCenter(this.createPos, data, rotation);

			this.blueprint = new Blueprint(world, bb.getMin(), data);
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

		funnel.setPos("p", this.createPos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.createPos = funnel.getPos("p");
	}
}
