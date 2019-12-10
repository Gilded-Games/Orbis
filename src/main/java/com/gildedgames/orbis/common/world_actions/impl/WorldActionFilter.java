package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.block.BlockFilter;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.core.ICreationData;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.BlueprintHelper;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionFilter extends WorldActionBase
{

	private BlockFilter filter;

	private IShape shapeToFilter;

	private boolean schedules;

	private BlockDataContainer history, oldState, newState;

	private WorldActionFilter()
	{

	}

	public WorldActionFilter(IShape shapeToFilter, BlockFilter filter, boolean schedules)
	{
		this.shapeToFilter = shapeToFilter;
		this.filter = filter;
		this.schedules = schedules;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		super.redo(player, world);

		this.history = BlueprintHelper.fetchBlocksInside(this.shapeToFilter, world);

		final ICreationData creationData = new CreationDataOrbis(world, player.getEntity()).seed(this.getSeed()).placesVoid(true);

		creationData.schedules(this.schedules);

		this.filter.apply(null, this.shapeToFilter, creationData.pos(this.shapeToFilter.getBoundingBox().getMin()),
				player.powers().getFillPower().getFilterOptions());

		//this.oldState = BlueprintHelper.fetchBlocksInside(this.shapeToFilter, world);

		/*if (this.history == null)
		{

		}
		else
		{
			DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

			BlockDataContainer currentState = BlueprintHelper.fetchBlocksInside(this.shapeToFilter, world);
			BlockDataContainer difference = BlueprintHelper.fetchDifferenceBetween(this.oldState, this.newState);

			primer.create(this.newState, new CreationData(world).pos(this.shapeToFilter.getBoundingBox().getMin()).placesVoid(true));

			for (BlockPos.MutableBlockPos pos : BlockPos
					.getAllInBoxMutable(BlockPos.ORIGIN, new BlockPos(currentState.getWidth() - 1, currentState.getHeight() - 1, currentState.getLength() - 1)))
			{
				IBlockState differenceState = difference.getBlockState(pos);

				if (differenceState != Blocks.STRUCTURE_VOID.getDefaultState() && currentState.getBlockState(pos) != differenceState)
				{
					world.setBlockState(pos.add(this.shapeToFilter.getBoundingBox().getMin()), this.oldState.getBlockState(pos));
				}
			}
		}*/
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		super.undo(player, world);

		//this.newState = BlueprintHelper.fetchBlocksInside(this.shapeToFilter, world);

		//BlockDataContainer difference = BlueprintHelper.fetchDifferenceBetween(this.oldState, this.newState);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));
		CreationData creationData = new CreationData(world).pos(this.shapeToFilter.getBoundingBox().getMin()).placesVoid(true);

		primer.create(this.history, creationData, this.shapeToFilter.getShapeData(), creationData.getPos());
		//primer.create(difference, new CreationData(world).pos(this.shapeToFilter.getBoundingBox().getMin()));
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

		funnel.set("f", this.filter);
		funnel.set("s", this.shapeToFilter);
		tag.setBoolean("sc", this.schedules);

	}

	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		this.filter = funnel.get("f");
		this.shapeToFilter = funnel.get("s");
		this.schedules = tag.getBoolean("sc");
	}
}
