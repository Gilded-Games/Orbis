package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.common.world_objects.GhostBlockDataContainer;
import com.gildedgames.orbis_api.block.BlockDataContainer;
import com.gildedgames.orbis_api.core.CreationData;
import com.gildedgames.orbis_api.core.ICreationData;
import com.gildedgames.orbis_api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis_api.processing.DataPrimer;
import com.gildedgames.orbis_api.util.BlueprintHelper;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.world.World;

public class WorldActionGenerateGhostBlockDataContainer implements IWorldAction
{

	private GhostBlockDataContainer ghostBlockDataContainer;

	private BlockDataContainer oldContent;

	private ICreationData creationData;

	private int worldObjectId;

	private WorldActionGenerateGhostBlockDataContainer()
	{

	}

	public WorldActionGenerateGhostBlockDataContainer(GhostBlockDataContainer ghostBlockDataContainer)
	{
		this.worldObjectId = WorldObjectManager.get(ghostBlockDataContainer.getWorld()).getID(ghostBlockDataContainer);
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (this.ghostBlockDataContainer == null)
		{
			this.ghostBlockDataContainer = WorldObjectManager.get(world).getObject(this.worldObjectId);
		}

		final Rotation rotation = player.powers().getBlueprintPower().getPlacingRotation();

		this.oldContent = BlueprintHelper.fetchBlocksInside(this.ghostBlockDataContainer, world);

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		if (this.creationData == null)
		{
			this.creationData = new CreationData(world, player.getEntity()).pos(this.ghostBlockDataContainer.getMin()).rotation(rotation)
					.placesAir(player.getCreationSettings().placesAirBlocks());
		}
		else
		{
			this.creationData.pos(this.ghostBlockDataContainer.getMin()).world(world).rotation(rotation);
		}

		primer.create(this.ghostBlockDataContainer, this.ghostBlockDataContainer.getBlockDataContainer(), this.creationData, null);
		WorldObjectManager.get(world).removeObject(this.ghostBlockDataContainer);
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (this.ghostBlockDataContainer == null)
		{
			this.ghostBlockDataContainer = WorldObjectManager.get(world).getObject(this.worldObjectId);
		}

		DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		primer.create(this.oldContent, new CreationData(world).pos(this.ghostBlockDataContainer.getBoundingBox().getMin()));
		WorldObjectManager.get(world).addObject(this.ghostBlockDataContainer);
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{

	}

	@Override
	public boolean isTemporary()
	{
		return true;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		tag.setInteger("i", this.worldObjectId);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		this.worldObjectId = tag.getInteger("i");
	}
}
