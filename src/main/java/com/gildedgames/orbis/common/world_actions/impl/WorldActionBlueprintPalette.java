package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis_api.block.BlockDataContainer;
import com.gildedgames.orbis_api.core.BakedBlueprint;
import com.gildedgames.orbis_api.core.CreationData;
import com.gildedgames.orbis_api.core.ICreationData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis_api.processing.DataPrimer;
import com.gildedgames.orbis_api.util.BlueprintHelper;
import com.gildedgames.orbis_api.util.RotationHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldActionBlueprintPalette extends WorldActionBase
{

	private BlockPos pos;

	private BlockDataContainer oldContent;

	private BlueprintData chosenBlueprint;

	private IRegion region;

	private WorldActionBlueprintPalette()
	{

	}

	public WorldActionBlueprintPalette(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		super.redo(player, world);

		final Rotation rotation = player.powers().getBlueprintPower().getPlacingRotation();
		ICreationData data = new CreationData(world, player.getEntity()).rotation(rotation).placesAir(false).seed(this.getSeed());

		if (this.chosenBlueprint == null)
		{
			this.chosenBlueprint = player.powers().getBlueprintPower().getPlacingPalette().fetchRandom(data.getWorld(), data.getRandom());
			this.region = RotationHelp.regionFromCenter(this.pos, this.chosenBlueprint, rotation);
		}

		this.oldContent = BlueprintHelper.fetchBlocksInside(this.region, world);

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		BakedBlueprint baked = new BakedBlueprint(this.chosenBlueprint, data.pos(this.region.getMin()));

		baked.bake();

		primer.create(this.region, baked);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		super.undo(player, world);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.oldContent, new CreationData(world).pos(this.region.getMin()));
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{

	}

	@Override
	public void write(NBTTagCompound tag)
	{
		super.write(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("p", this.pos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		this.pos = funnel.getPos("p");
	}
}
