package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.world.World;

public interface IWorldAction extends NBT
{

	void redo(PlayerOrbis player, World world);

	void undo(PlayerOrbis player, World world);

}
