package com.gildedgames.orbis.common;

import com.gildedgames.orbis.OrbisAPI;
import com.gildedgames.orbis.common.data_packs.OrbisDataPackManager;
import com.gildedgames.orbis.common.data_packs.OrbisPackDataShapeType;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceFactory;
import com.gildedgames.orbis.common.world.orbis_instance.OrbisInstanceHandler;
import com.gildedgames.orbis.common.world.orbis_instance.WorldProviderOrbis;
import com.gildedgames.orbis_api.OrbisLib;
import com.gildedgames.orbis_api.filetransfer.FileReceiver;
import com.gildedgames.orbis_api.filetransfer.FileTransferTracker;
import com.gildedgames.orbis_api.filetransfer.IFileTransferTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

import java.io.File;
import java.util.Optional;

public class CommonProxy
{

	public static final ResourceLocation PACKS_TRANSFER_TRACKER = OrbisCore.getResource("packs");

	protected OrbisDataPackManager dataPackManager;

	public OrbisDataPackManager getDataPackManager()
	{
		return this.dataPackManager;
	}

	public IFileTransferTracker getFTT()
	{
		Optional<IFileTransferTracker> tracker = OrbisLib.PROXY.getFileTransferManager().getTracker(PACKS_TRANSFER_TRACKER);

		return tracker.orElse(null);
	}

	protected void createDataPackManager()
	{
		this.dataPackManager = new OrbisDataPackManager(new File(DimensionManager.getCurrentSaveRootDirectory(), "/orbis/packs/"));
	}

	protected void registerFileTransferTracker()
	{
		OrbisLib.PROXY.getFileTransferManager()
				.registerTracker(PACKS_TRANSFER_TRACKER,
						() -> new FileTransferTracker(PACKS_TRANSFER_TRACKER, new FileReceiver(this.dataPackManager.getBaseDirectory())));
	}

	public void preInit(FMLPreInitializationEvent event)
	{
		OrbisLib.services().setProjectManagerInitSource(OrbisCore.INSTANCE, "orbis");

		WorldProviderOrbis.preInit();

		final OrbisInstanceFactory factory = new OrbisInstanceFactory(WorldProviderOrbis.ORBIS);

		OrbisCore.ORBIS_INSTANCE_HANDLER = new OrbisInstanceHandler(OrbisLib.instances().createInstanceHandler(factory));
	}

	public void init(FMLInitializationEvent event)
	{
		this.registerFileTransferTracker();
		OrbisAPI.services().registerPackDataType("shape", OrbisPackDataShapeType.class, new OrbisPackDataShapeType.Deserializer());
	}

	public void stopDataPackManager()
	{
		if (this.dataPackManager != null)
		{
			this.dataPackManager.stop();

			this.dataPackManager = null;
		}
	}

	public void startDataPackManager()
	{
		if (this.dataPackManager != null)
		{
			return;
		}

		this.createDataPackManager();

		this.dataPackManager.start();
	}

	public void onServerStarted(final FMLServerStartedEvent event)
	{
		this.startDataPackManager();
	}

	public void onServerStopped(final FMLServerStoppedEvent event)
	{
		this.stopDataPackManager();
	}

	public EntityPlayer getPlayer()
	{
		return null;
	}
}
