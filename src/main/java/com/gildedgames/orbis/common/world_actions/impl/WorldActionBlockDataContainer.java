package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis_api.block.BlockDataContainer;
import com.gildedgames.orbis_api.core.CreationData;
import com.gildedgames.orbis_api.core.ICreationData;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis_api.processing.DataPrimer;
import com.gildedgames.orbis_api.util.BlueprintHelper;
import com.gildedgames.orbis_api.util.RotationHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class WorldActionBlockDataContainer implements IWorldAction
{

	private ItemStack containerStack;

	private BlockPos pos;

	private BlockDataContainer oldContent;

	private IRegion bb;

	private ICreationData creationData;

	private WorldActionBlockDataContainer()
	{

	}

	public WorldActionBlockDataContainer(ItemStack containerStack, BlockPos pos)
	{
		this.containerStack = containerStack;
		this.pos = pos;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		final Optional<BlockDataContainer> container = ItemBlockDataContainer.getDataContainer(this.containerStack);

		if (container.isPresent())
		{
			final Rotation rotation = player.powers().getBlueprintPower().getPlacingRotation();

			if (this.bb == null)
			{
				this.bb = RotationHelp.regionFromCenter(this.pos, container.get(), rotation);
			}

			this.oldContent = BlueprintHelper.fetchBlocksInside(this.bb, world);

			final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

			if (this.creationData == null)
			{
				this.creationData = new CreationData(world, player.getEntity()).pos(this.bb.getMin()).rotation(rotation)
						.placesAir(player.getCreationSettings().placesAirBlocks());
			}
			else
			{
				this.creationData.pos(this.bb.getMin()).world(world).rotation(rotation);
			}

			primer.create(this.bb, container.get(), this.creationData, null);
		}
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
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
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setStack("s", this.containerStack);
		funnel.setPos("p", this.pos);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.containerStack = funnel.getStack("s");
		this.pos = funnel.getPos("p");
	}
}
