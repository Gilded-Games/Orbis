package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.core.ICreationData;
import com.gildedgames.orbis.lib.core.baking.BakedBlueprint;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.BlueprintHelper;
import com.gildedgames.orbis.lib.util.RotationHelp;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
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

	private BakedBlueprint baked;

	private ICreationData creationData;

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

		if (this.creationData == null)
		{
			this.creationData = new CreationData(world, player.getEntity()).rotation(rotation).placesAir(player.getCreationSettings().placesAirBlocks())
					.seed(this.getSeed());
		}
		else
		{
			this.creationData.rotation(rotation).creator(player.getEntity());
		}

		if (this.chosenBlueprint == null)
		{
			this.chosenBlueprint = player.powers().getBlueprintPower().getPlacingPalette()
					.fetchRandom(this.creationData.getWorld(), this.creationData.getRandom());
			this.region = RotationHelp.regionFromCenter(this.pos, this.chosenBlueprint, rotation);
		}

		this.oldContent = BlueprintHelper.fetchBlocksInside(this.region, world);

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		if (this.baked == null)
		{
			this.baked = new BakedBlueprint(this.chosenBlueprint, this.creationData.pos(this.region.getMin()));
		}

		primer.place(this.baked);
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
