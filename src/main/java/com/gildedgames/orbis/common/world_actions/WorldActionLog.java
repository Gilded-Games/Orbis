package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.api.util.FixedStack;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.world.World;

public class WorldActionLog implements IWorldActionLog
{
	private int historySize;

	/**
	 * The actions that have been created
	 */
	private FixedStack<IWorldAction> past;

	/**
	 * The actions that have been called with "undo"
	 */
	private FixedStack<IWorldAction> future;

	private PlayerOrbis playerOrbis;

	public WorldActionLog(PlayerOrbis playerOrbis, int historySize)
	{
		this.playerOrbis = playerOrbis;
		this.historySize = historySize;

		this.past = new FixedStack<>(this.historySize);
		this.future = new FixedStack<>(this.historySize);
	}

	/**
	 * When an action is tracked, the future stack is cleared
	 * meaning undo actions can no longer be redone
	 * @param action The tracked action
	 */
	@Override
	public void track(World world, IWorldAction action)
	{
		if (action == null)
		{
			return;
		}

		this.future.clear();

		this.past.push(action);

		action.redo(this.playerOrbis, world);
	}

	@Override
	public void undo(World world)
	{
		if (!this.past.isEmpty())
		{
			IWorldAction action = this.past.pop();

			if (action != null)
			{
				action.undo(this.playerOrbis, world);

				this.future.push(action);
			}
		}
	}

	@Override
	public void redo(World world)
	{
		if (!this.future.isEmpty())
		{
			IWorldAction action = this.future.pop();

			if (action != null)
			{
				action.redo(this.playerOrbis, world);

				this.past.push(action);
			}
		}
	}

	@Override
	public void clear()
	{
		this.past.clear();
		this.future.clear();
	}

}
