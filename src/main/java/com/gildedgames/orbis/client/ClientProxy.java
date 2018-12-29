package com.gildedgames.orbis.client;

import com.gildedgames.orbis.client.model.FakeOrbisFloorWorld;
import com.gildedgames.orbis.client.model.ModelOrbisFloor;
import com.gildedgames.orbis.common.CommonProxy;
import com.gildedgames.orbis.common.blocks.BlocksOrbis;
import com.gildedgames.orbis.common.data_packs.OrbisDataPackManager;
import com.gildedgames.orbis_api.OrbisLib;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.io.File;

public class ClientProxy extends CommonProxy
{

	@Override
	protected void createDataPackManager()
	{
		final ServerData data = Minecraft.getMinecraft().getCurrentServerData();

		if (data != null)
		{
			this.dataPackManager = new OrbisDataPackManager(
					new File(Minecraft.getMinecraft().mcDataDir, "/orbis/servers/" + data.serverIP.replace(":", "_") + "/packs/"));
		}
		else
		{
			this.dataPackManager = new OrbisDataPackManager(new File(Minecraft.getMinecraft().mcDataDir, "/orbis/local/packs/"));
		}
	}

	@Override
	public void init(final FMLInitializationEvent event)
	{
		super.init(event);

		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) ->
		{
			if (ModelOrbisFloor.currentMimicBlock != null && ModelOrbisFloor.currentMimicBlock.getBlock() != BlocksOrbis.orbis_floor)
			{
				return Minecraft.getMinecraft().getBlockColors()
						.colorMultiplier(ModelOrbisFloor.currentMimicBlock, new FakeOrbisFloorWorld(ModelOrbisFloor.currentMimicBlock), pos, tintIndex);
			}
			else
			{
				return ModelOrbisFloor.color;
			}
		}, BlocksOrbis.orbis_floor);

		OrbisKeyBindings.init();

		OrbisLib.services().lootTableCache().attachReloadListener();
	}

	@Override
	public EntityPlayer getPlayer()
	{
		return FMLClientHandler.instance().getClientPlayerEntity();
	}
}