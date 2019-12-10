package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.util.mc.NBT;
import net.minecraft.world.World;

public interface IWorldAction extends NBT
{

	void redo(PlayerOrbis player, World world);

	void undo(PlayerOrbis player, World world);

	void setWorld(PlayerOrbis playerOrbis, World world);

	/**
	 * Will only stay on the action history for one action.
	 * As soon as another action is made, this action is lost
	 * from the action history.
	 */
	boolean isTemporary();

}
