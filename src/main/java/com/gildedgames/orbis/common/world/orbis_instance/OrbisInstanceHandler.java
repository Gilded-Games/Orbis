package com.gildedgames.orbis.common.world.orbis_instance;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.lib.OrbisLib;
import com.gildedgames.orbis.lib.util.mc.BlockPosDimension;
import com.gildedgames.orbis.lib.world.instances.IInstanceHandler;
import com.gildedgames.orbis.lib.world.instances.IPlayerInstances;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public class OrbisInstanceHandler
{

	private final IInstanceHandler<OrbisInstance> handler;

	public OrbisInstanceHandler(final IInstanceHandler<OrbisInstance> handler)
	{
		this.handler = handler;
	}

	public void registerInstance(OrbisInstance instance)
	{
		this.handler.registerInstance(instance);
	}

	public OrbisInstance getFromDimId(final int dimId)
	{
		return this.handler.getInstanceForDimension(dimId);
	}

	public OrbisInstance get(PlayerOrbis playerOrbis, BlockPosDimension entrance)
	{
		if (playerOrbis.getOrbisInstance() != null)
		{
			return playerOrbis.getOrbisInstance();
		}

		final OrbisInstance inst = this.handler.createNew();
		inst.setOutsideEntrance(entrance);

		playerOrbis.setOrbisInstance(inst);

		return inst;
	}

	public void teleportToInst(final EntityPlayerMP player, final OrbisInstance inst)
	{
		final World world = this.handler.teleportPlayerToInstance(inst, player);

		player.connection.setPlayerLocation(inst.getInsideEntrance().getX(), inst.getInsideEntrance().getY(), inst.getInsideEntrance().getZ(), 180, 0);

		inst.onJoin(player);
	}

	public void teleportBack(final EntityPlayerMP player)
	{
		final IPlayerInstances hook = OrbisLib.instances().getPlayer(player);

		if (hook.getInstance() != null)
		{
			hook.getInstance().onLeave(player);
		}

		this.handler.returnPlayerFromInstance(player);

		hook.setInstance(null);
	}
}
