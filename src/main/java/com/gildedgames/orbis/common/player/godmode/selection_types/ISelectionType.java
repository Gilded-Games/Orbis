package com.gildedgames.orbis.common.player.godmode.selection_types;

import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.util.mc.NBT;
import com.gildedgames.orbis.client.godmode.selection_types.ISelectionTypeClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.util.math.BlockPos;

public interface ISelectionType extends NBT
{

	ISelectionTypeClient getClient();

	IShape createShape(BlockPos start, BlockPos end, PlayerOrbis playerOrbis, boolean centered);

}
