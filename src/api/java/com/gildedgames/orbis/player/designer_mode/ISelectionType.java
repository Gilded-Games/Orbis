package com.gildedgames.orbis.player.designer_mode;

import com.gildedgames.orbis.client.ISelectionTypeClient;
import com.gildedgames.orbis.player.IPlayerOrbis;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.util.mc.NBT;
import net.minecraft.util.math.BlockPos;

public interface ISelectionType extends NBT
{

	ISelectionTypeClient getClient();

	IShape createShape(BlockPos start, BlockPos end, IPlayerOrbis playerOrbis, boolean centered);

}
