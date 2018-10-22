package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.util.FixedStack;
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

	private String worldActionLogId;

	public WorldActionLog(String worldActionLogId, PlayerOrbis playerOrbis, int historySize)
	{
		this.worldActionLogId = worldActionLogId;
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
	public void apply(World world, IWorldAction action)
	{
		this.track(world, action);

		action.redo(this.playerOrbis, world);
	}

	@Override
	public void track(World world, IWorldAction action)
	{
		if (action == null)
		{
			return;
		}

		this.future.clear();

		this.past.push(action);

		action.setWorld(this.playerOrbis, world);
	}

	@Override
	public void undo(World world)
	{
		if (!this.past.isEmpty())
		{
			IWorldAction action = this.past.pop();

			if (action != null)
			{
				action.setWorld(this.playerOrbis, world);

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
				action.setWorld(this.playerOrbis, world);

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
