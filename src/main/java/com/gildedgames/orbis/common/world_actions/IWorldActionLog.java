package com.gildedgames.orbis.common.world_actions;

import net.minecraft.world.World;

/**
 * A log which keeps track of all world actions
 * made by a player, then allows you to redo and undo
 * those actions.
 */
public interface IWorldActionLog
{

	/**
	 * Tracks and applies the action.
	 * @param action The tracked action.
	 */
	void track(World world, IWorldAction action);

	void redo(World world);

	void undo(World world);

	void clear();

}
