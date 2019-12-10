package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.GodPowerPathway;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.core.CreationData;
import com.gildedgames.orbis.lib.core.ICreationData;
import com.gildedgames.orbis.lib.core.baking.BakedBlueprint;
import com.gildedgames.orbis.lib.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis.lib.data.framework.generation.searching.PathwayNode;
import com.gildedgames.orbis.lib.data.framework.generation.searching.PathwayProblem;
import com.gildedgames.orbis.lib.data.framework.generation.searching.StepAStar;
import com.gildedgames.orbis.lib.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.lib.processing.DataPrimer;
import com.gildedgames.orbis.lib.util.BlueprintHelper;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class WorldActionPathway implements IWorldAction
{

	private BlockPos start, end;

	private List<Pair<BlockPos, BlockDataContainer>> oldContent = Lists.newArrayList();

	private PathwayProblem problem;

	private StepAStar<PathwayNode> stepAStar;

	private BlueprintRegion initialNode;

	private ICreationData creationData;

	private WorldActionPathway()
	{

	}

	public WorldActionPathway(BlockPos start, BlockPos end)
	{
		this.start = start;
		this.end = end;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		if (this.problem == null)
		{
			GodPowerPathway p = player.powers().getPathwayPower();

			p.processPathway(player, this.start, this.end, true);

			this.problem = p.getPathwayProblem();
			this.stepAStar = p.getStepAStar();
			this.initialNode = p.getInitialNode();
		}

		this.oldContent.clear();

		if (this.creationData == null)
		{
			this.creationData = new CreationData(world, player.getEntity()).placesAir(player.getCreationSettings().placesAirBlocks());
		}

		for (PathwayNode n : this.stepAStar.currentState().fullPath())
		{
			this.oldContent.add(Pair.of(n.getMin(), BlueprintHelper.fetchBlocksInside(n, world)));

			this.creationData.pos(n.getMin()).rotation(n.getRotation()).creator(player.getEntity());

			BakedBlueprint baked = new BakedBlueprint(n.getData(), this.creationData);

			primer.place(baked);
		}
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		for (Pair<BlockPos, BlockDataContainer> pair : this.oldContent)
		{
			primer.create(pair.getValue(), new CreationData(world).pos(pair.getKey()));
		}
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{

	}

	@Override
	public boolean isTemporary()
	{
		return false;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setBlockPos("s", this.start);
		funnel.setBlockPos("e", this.end);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.start = funnel.get("s");
		this.end = funnel.get("e");
	}
}
