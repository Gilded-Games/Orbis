package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.block.BlockFilter;
import com.gildedgames.orbis.lib.block.BlockFilterHelper;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.core.ICreationData;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.data.region.IShape;
import com.gildedgames.orbis.lib.data.region.Region;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.BlueprintHelper;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Set;

public class WorldActionFilterMultiple extends WorldActionBase
{

	private BlockFilter filter;

	private IShape shapeToFilter;

	private boolean schedules;

	private List<Pair<BlockDataContainer, BlockPos>> oldContent = Lists.newArrayList();

	private Set<BlockPos> multiplePositions;

	private WorldActionFilterMultiple()
	{

	}

	public WorldActionFilterMultiple(IShape shapeToFilter, BlockFilter filter, boolean schedules, Set<BlockPos> multiplePositions)
	{
		this.shapeToFilter = shapeToFilter;
		this.filter = filter;
		this.schedules = schedules;
		this.multiplePositions = multiplePositions;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		super.redo(player, world);

		this.oldContent.clear();

		int width = this.shapeToFilter.getBoundingBox().getWidth();
		int height = this.shapeToFilter.getBoundingBox().getHeight();
		int length = this.shapeToFilter.getBoundingBox().getLength();

		for (BlockPos p : this.multiplePositions)
		{
			IRegion bb = new Region(p, p.add(width, height, length));

			BlockDataContainer container = BlueprintHelper.fetchBlocksInside(Blocks.STRUCTURE_VOID.getDefaultState(), bb, world, null);

			this.oldContent.add(Pair.of(container, p));
		}

		final ICreationData creationData = new CreationDataOrbis(world, player.getEntity()).seed(this.getSeed()).placesVoid(true);

		creationData.schedules(this.schedules);

		for (BlockPos pos : this.multiplePositions)
		{
			this.filter.apply(null, this.shapeToFilter, creationData.pos(pos), player.powers().getFillPower().getFilterOptions());
		}
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		super.undo(player, world);

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		final ICreationData creationData = new CreationDataOrbis(world, player.getEntity()).seed(this.getSeed());

		BlockFilter filter = new BlockFilterHelper.BlockDeleteFilter();

		for (BlockPos pos : this.multiplePositions)
		{
			filter.apply(null, this.shapeToFilter, creationData.pos(pos), player.powers().getFillPower().getFilterOptions());
		}

		for (Pair<BlockDataContainer, BlockPos> pair : this.oldContent)
		{
			primer.create(pair.getLeft(), new CreationData(world).pos(pair.getRight()).placesAir(false));
		}
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
		funnel.setSet("p", this.multiplePositions, NBTFunnel.POS_SETTER);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		this.filter = funnel.get("f");
		this.shapeToFilter = funnel.get("s");
		this.schedules = tag.getBoolean("sc");
		this.multiplePositions = funnel.getSet("p", NBTFunnel.POS_GETTER);
	}
}
