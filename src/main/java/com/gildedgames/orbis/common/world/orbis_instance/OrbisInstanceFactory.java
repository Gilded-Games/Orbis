package com.gildedgames.orbis.common.world.orbis_instance;

import com.gildedgames.orbis_api.util.TeleporterGeneric;
import com.gildedgames.orbis_api.world.instances.IInstanceFactory;
import com.gildedgames.orbis_api.world.instances.IInstanceHandler;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class OrbisInstanceFactory implements IInstanceFactory<OrbisInstance>
{

	private final DimensionType dimensionType;

	public OrbisInstanceFactory(final DimensionType dimensionType)
	{
		this.dimensionType = dimensionType;
	}

	@Override
	public OrbisInstance createInstance(final int dimId, final IInstanceHandler instanceHandler)
	{
		return new OrbisInstance(dimId);
	}

	@Override
	public DimensionType dimensionType()
	{
		return this.dimensionType;
	}

	@Override
	public Teleporter getTeleporter(final WorldServer worldIn)
	{
		return new TeleporterGeneric(worldIn);
	}

}
