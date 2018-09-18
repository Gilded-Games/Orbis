package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.client.godmode.GodPowerPathwayClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorPathway;
import com.gildedgames.orbis_api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.framework.generation.searching.PathwayNode;
import com.gildedgames.orbis_api.data.framework.generation.searching.PathwayProblem;
import com.gildedgames.orbis_api.data.framework.generation.searching.StepAStar;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public class GodPowerPathway implements IGodPower
{

	private final ShapeSelectorPathway shapeSelector;

	private GodPowerPathwayClient clientHandler;

	private StepAStar<PathwayNode> stepAStar;

	private PathwayProblem pathwayProblem;

	private BlueprintRegion initialNode;

	public GodPowerPathway(final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerPathwayClient();
		}

		this.shapeSelector = new ShapeSelectorPathway(this);
	}

	public static <T> T random(Collection<T> coll)
	{
		int num = (int) (Math.random() * coll.size());
		for (T t : coll)
		{
			if (--num < 0)
			{
				return t;
			}
		}
		throw new AssertionError();
	}

	public BlueprintRegion getInitialNode()
	{
		return this.initialNode;
	}

	public StepAStar<PathwayNode> getStepAStar()
	{
		return this.stepAStar;
	}

	public PathwayProblem getPathwayProblem()
	{
		return this.pathwayProblem;
	}

	public void processPathway(PlayerOrbis playerOrbis, BlockPos start, BlockPos end, boolean finishImmediately)
	{
		Collection<BlueprintData> pieces = this.getActivePieces(playerOrbis);

		BlueprintData initialData = random(pieces);

		this.initialNode = new BlueprintRegion(start, initialData);

		this.pathwayProblem = new PathwayProblem(playerOrbis.getWorld(), start, this.initialNode, end, Lists.newArrayList(pieces),
				Collections.emptyList());
		this.stepAStar = new StepAStar<>(this.pathwayProblem, 1.2F);

		if (finishImmediately)
		{
			while (!this.stepAStar.isTerminated())
			{
				this.stepAStar.step();
			}
		}
	}

	@Override
	public void onUpdate(final EntityPlayer player, final PlayerOrbis playerOrbis, final boolean isPowerActive)
	{
		if (player.world.isRemote && this.stepAStar != null)
		{
			if (!this.stepAStar.isTerminated())
			{
				for (int step = 0; step < 1; step++)
				{
					this.stepAStar.step();
				}
			}
		}
	}

	@Override
	public boolean hasCustomGui(PlayerOrbis playerOrbis)
	{
		return false;
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public boolean canInteractWithItems(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty() && (held.getItem() instanceof ItemBlock || held.getItem() instanceof ItemBlockDataContainer);
	}

	@Override
	public IShapeSelector getShapeSelector()
	{
		return this.shapeSelector;
	}

	@Override
	public IGodPowerClient getClientHandler()
	{
		return this.clientHandler;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	public Collection<BlueprintData> getActivePieces(PlayerOrbis playerOrbis)
	{
		Collection<BlueprintData> pieces = Collections.emptyList();

		if (playerOrbis.powers().getBlueprintPower().getPlacingPalette() != null)
		{
			pieces = playerOrbis.powers().getBlueprintPower().getPlacingPalette().getData();
		}
		else if (playerOrbis.powers().getBlueprintPower().getPlacingBlueprint() != null)
		{
			pieces = Lists.newArrayList(playerOrbis.powers().getBlueprintPower().getPlacingBlueprint());
		}

		return pieces;
	}

}
