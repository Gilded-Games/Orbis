package com.gildedgames.orbis.api.world.instances;

import com.gildedgames.orbis.api.OrbisAPICapabilities;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.UUID;

public class InstanceRegistryImpl implements IInstanceRegistry
{

	private List<IInstanceHandler> instances;

	public InstanceRegistryImpl()
	{

	}

	@Override
	public List<IInstanceHandler> getInstanceHandlers()
	{
		if (this.instances == null)
		{
			this.instances = Lists.newArrayList();
		}

		return this.instances;
	}

	@Override
	public void registerInstanceHandler(final IInstanceHandler handler)
	{
		this.getInstanceHandlers().add(handler);
	}

	@Override
	public <T extends IInstance> InstanceHandler<T> createAndRegisterInstanceHandler(final IInstanceFactory<T> factory)
	{
		final InstanceHandler<T> handler = new InstanceHandler<>(factory);
		this.registerInstanceHandler(handler);

		return handler;
	}

	@Override
	public IPlayerInstances getPlayer(final EntityPlayer player)
	{
		return player.getCapability(OrbisAPICapabilities.PLAYER_INSTANCES, null);
	}

	@Override
	public IPlayerInstances getPlayer(final UUID uuid)
	{
		final EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);

		if (player == null)
		{
			return null;
		}

		return this.getPlayer(player);
	}

}
