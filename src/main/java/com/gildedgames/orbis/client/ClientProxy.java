package com.gildedgames.orbis.client;

import com.gildedgames.orbis.common.CommonProxy;
import com.gildedgames.orbis_api.OrbisAPI;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy
{
	@Override
	public void init(final FMLInitializationEvent event)
	{
		super.init(event);

		OrbisKeyBindings.init();

		OrbisAPI.services().lootTableCache().attachReloadListener();
	}
}
