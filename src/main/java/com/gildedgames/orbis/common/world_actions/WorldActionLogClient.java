package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.world_actions.*;
import net.minecraft.world.World;

public class WorldActionLogClient implements IWorldActionLog
{
	private String worldActionLogId;

	public WorldActionLogClient(String worldActionLogId)
	{
		this.worldActionLogId = worldActionLogId;
	}

	@Override
	public void apply(World world, IWorldAction action)
	{
		if (action == null)
		{
			return;
		}

		OrbisCore.network().sendPacketToServer(new PacketApplyWorldAction(this.worldActionLogId, action));
	}

	@Override
	public void track(World world, IWorldAction action)
	{
		if (action == null)
		{
			return;
		}

		OrbisCore.network().sendPacketToServer(new PacketTrackWorldAction(this.worldActionLogId, action));
	}

	@Override
	public void redo(World world)
	{
		OrbisCore.network().sendPacketToServer(new PacketRedoWorldAction(this.worldActionLogId));
	}

	@Override
	public void undo(World world)
	{
		OrbisCore.network().sendPacketToServer(new PacketUndoWorldAction(this.worldActionLogId));
	}

	@Override
	public void clear()
	{
		OrbisCore.network().sendPacketToServer(new PacketClearWorldActions(this.worldActionLogId));
	}
}
