package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.world.World;

import java.util.Stack;

public class WorldActionLog implements IWorldActionLog
{

	/**
	 * The actions that have been created
	 */
	private Stack<IWorldAction> past = new Stack<>();

	/**
	 * The actions that have been called with "undo"
	 */
	private Stack<IWorldAction> future = new Stack<>();

	private PlayerOrbis playerOrbis;

	public WorldActionLog(PlayerOrbis playerOrbis)
	{
		this.playerOrbis = playerOrbis;
	}

	/**
	 * When an action is tracked, the future stack is cleared
	 * meaning undo actions can no longer be redone
	 * @param action The tracked action
	 */
	@Override
	public void track(World world, IWorldAction action)
	{
		this.future.clear();
		this.past.push(action);

		action.redo(this.playerOrbis, world);
	}

	@Override
	public void undo(World world)
	{
		if (!this.past.isEmpty() && this.past.peek() != null)
		{
			IWorldAction action = this.past.pop();

			action.undo(this.playerOrbis, world);

			this.future.push(action);
		}
	}

	@Override
	public void redo(World world)
	{
		if (!this.future.isEmpty() && this.future.peek() != null)
		{
			IWorldAction action = this.future.pop();

			action.redo(this.playerOrbis, world);

			this.past.push(action);
		}
	}

	@Override
	public void clear()
	{
		this.past.clear();
		this.future.clear();
	}

}
