package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis_api.OrbisAPI;
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

		ICreationData creationData = new CreationData(world, player.getEntity()).pos(this.pos.add(-this.data.getWidth() / 2, 0, -this.data.getLength() / 2))
				.rotation(rotation).placesAir(false)
				.seed(this.getSeed());

		BakedBlueprint baked = new BakedBlueprint(this.data, creationData);

		baked.bake();

		primer.create(this.bb, baked);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		super.undo(player, world);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.oldContent, new CreationData(world).pos(this.bb.getBoundingBox().getMin()));
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
