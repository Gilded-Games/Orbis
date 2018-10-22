package com.gildedgames.orbis.client;

import com.gildedgames.orbis.client.model.FakeOrbisFloorWorld;
import com.gildedgames.orbis.client.model.ModelOrbisFloor;
import com.gildedgames.orbis.common.CommonProxy;
import com.gildedgames.orbis.common.blocks.BlocksOrbis;
import com.gildedgames.orbis_api.OrbisAPI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy
{

	@Override
	public void init(final FMLInitializationEvent event)
	{
		super.init(event);

		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) ->
		{
			if (ModelOrbisFloor.currentMimicBlock.getBlock() != BlocksOrbis.orbis_floor)
			{
				return Minecraft.getMinecraft().getBlockColors().colorMultiplier(ModelOrbisFloor.currentMimicBlock, new FakeOrbisFloorWorld(ModelOrbisFloor.currentMimicBlock), pos, tintIndex);
			} else
			{
				return ModelOrbisFloor.color;
			}
		}, BlocksOrbis.orbis_floor);

		OrbisKeyBindings.init();

		OrbisAPI.services().lootTableCache().attachReloadListener();
	}
}