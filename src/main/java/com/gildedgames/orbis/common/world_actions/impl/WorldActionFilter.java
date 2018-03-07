package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.core.CreationData;
import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.BlueprintHelper;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionFilter implements IWorldAction
{

	private BlockFilter filter;

	private IShape shapeToFilter;

	private boolean schedules;

	private BlockDataContainer oldContent;

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
		this.oldContent = BlueprintHelper.fetchBlocksInside(this.shapeToFilter, world);

		final ICreationData creationData = new CreationDataOrbis(world, player.getEntity());

		creationData.schedules(this.schedules);

		this.filter.apply(this.shapeToFilter, creationData, player.powers().getFillPower().getFilterOptions());
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.oldContent, new CreationData(world).pos(this.shapeToFilter.getBoundingBox().getMin()));
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("f", this.filter);
		funnel.set("s", this.shapeToFilter);
		tag.setBoolean("sc", this.schedules);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.filter = funnel.get("f");
		this.shapeToFilter = funnel.get("s");
		this.schedules = tag.getBoolean("sc");
	}
}
