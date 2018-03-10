package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.BlueprintHelper;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class WorldActionBlueprint extends WorldActionBase
{

	private BlueprintData data;

	private BlockPos pos;

	private BlockDataContainer oldContent;

	private IRegion bb;

	private WorldActionBlueprint()
	{

	}

	public WorldActionBlueprint(BlueprintData data, BlockPos pos)
	{
		this.data = data;
		this.pos = pos;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		super.redo(player, world);

		final Rotation rotation = player.powers().getBlueprintPower().getPlacingRotation();

		if (this.bb == null)
		{
			this.bb = RotationHelp.regionFromCenter(this.pos, this.data, rotation);
		}

		this.oldContent = BlueprintHelper.fetchBlocksInside(this.bb, world);

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		ICreationData creationData = new CreationData(world, player.getEntity()).pos(this.bb.getMin()).rotation(rotation).placesAir(false)
				.rand(new Random(this.getSeed()));

		primer.create(this.bb, this.data, creationData);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		super.undo(player, world);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.oldContent, new CreationData(world).pos(this.bb.getBoundingBox().getMin()));
	}

	@Override
	public void setWorld(World world)
	{

	}

	@Override
	public void write(NBTTagCompound tag)
	{
		super.write(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("d", this.data.getMetadata().getIdentifier());
		funnel.setPos("p", this.pos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		this.data = OrbisAPI.services().getProjectManager().findData(funnel.get("d"));
		this.pos = funnel.getPos("p");
	}
}
