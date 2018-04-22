package com.gildedgames.orbis.common.world_actions;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketClearWorldActions;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketRedoWorldAction;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketTrackWorldAction;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketUndoWorldAction;
import com.gildedgames.orbis_api.OrbisAPI;
import net.minecraft.world.World;

public class WorldActionLogClient implements IWorldActionLog
{
	@Override
	public void track(World world, IWorldAction action)
	{
		if (action == null)
		{
			return;
		}

		OrbisCore.network().sendPacketToServer(new PacketTrackWorldAction(action));
	}

	@Override
	public void redo(World world)
	{
		OrbisCore.network().sendPacketToServer(new PacketRedoWorldAction());
	}

	@Override
	public void undo(World world)
	{
		OrbisCore.network().sendPacketToServer(new PacketUndoWorldAction());
	}

	@Override
	public void clear()
	{
		OrbisCore.network().sendPacketToServer(new PacketClearWorldActions());
	}
}
