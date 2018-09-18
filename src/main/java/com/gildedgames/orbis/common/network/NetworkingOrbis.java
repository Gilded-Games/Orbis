package com.gildedgames.orbis.common.network;

import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.*;
import com.gildedgames.orbis.common.network.packets.blueprints.*;
import com.gildedgames.orbis.common.network.packets.framework.PacketAddNode;
import com.gildedgames.orbis.common.network.packets.framework.PacketRemoveNode;
import com.gildedgames.orbis.common.network.packets.gui.*;
import com.gildedgames.orbis.common.network.packets.projects.*;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketClearWorldActions;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketRedoWorldAction;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketTrackWorldAction;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketUndoWorldAction;
import com.gildedgames.orbis_api.network.INetworkMultipleParts;
import com.gildedgames.orbis_api.network.NetworkMultipleParts;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkingOrbis
{
	private static INetworkMultipleParts network = new NetworkMultipleParts(OrbisCore.MOD_ID);

	public static INetworkMultipleParts network()
	{
		return network;
	}

	public static void preInit()
	{
		// S E R V E R
		network.reg(PacketWorldObjectAdd.HandlerServer.class, PacketWorldObjectAdd.class, Side.SERVER);
		network.reg(PacketWorldObjectRemove.HandlerServer.class, PacketWorldObjectRemove.class, Side.SERVER);
		network.reg(PacketActiveSelection.HandlerServer.class, PacketActiveSelection.class, Side.SERVER);
		network.reg(PacketActiveSelectionMultiple.HandlerServer.class, PacketActiveSelectionMultiple.class, Side.SERVER);
		network.reg(PacketChangePower.HandlerServer.class, PacketChangePower.class, Side.SERVER);
		network.reg(PacketOpenPowerGui.HandlerServer.class, PacketOpenPowerGui.class, Side.SERVER);
		network.reg(PacketSetItemStack.HandlerServer.class, PacketSetItemStack.class, Side.SERVER);
		network.reg(PacketChangeSelectionType.HandlerServer.class, PacketChangeSelectionType.class, Side.SERVER);
		network.reg(PacketFilterShape.HandlerServer.class, PacketFilterShape.class, Side.SERVER);
		network.reg(PacketRequestProject.HandlerServer.class, PacketRequestProject.class, Side.SERVER);
		network.reg(PacketRequestProjectListing.HandlerServer.class, PacketRequestProjectListing.class, Side.SERVER);
		network.reg(PacketSaveWorldObjectToProject.HandlerServer.class, PacketSaveWorldObjectToProject.class, Side.SERVER);
		network.reg(PacketRequestCreateProject.HandlerServer.class, PacketRequestCreateProject.class, Side.SERVER);
		network.reg(PacketDeleteFile.HandlerServer.class, PacketDeleteFile.class, Side.SERVER);
		network.reg(PacketClearSelection.HandlerServer.class, PacketClearSelection.class, Side.SERVER);
		network.reg(PacketRotateBlueprint.HandlerServer.class, PacketRotateBlueprint.class, Side.SERVER);
		network.reg(PacketClearSelectedRegion.HandlerServer.class, PacketClearSelectedRegion.class, Side.SERVER);
		network.reg(PacketSetItemStackInHand.HandlerServer.class, PacketSetItemStackInHand.class, Side.SERVER);
		network.reg(PacketSetBlockDataContainerInHand.HandlerServer.class, PacketSetBlockDataContainerInHand.class, Side.SERVER);
		network.reg(PacketChangeSelectionInput.HandlerServer.class, PacketChangeSelectionInput.class, Side.SERVER);
		network.reg(PacketCreatePlacingBlueprint.HandlerServer.class, PacketCreatePlacingBlueprint.class, Side.SERVER);
		network.reg(PacketCreateItemBlockDataContainer.HandlerServer.class, PacketCreateItemBlockDataContainer.class, Side.SERVER);
		network.reg(PacketCreatePlacingBlueprintPalette.HandlerServer.class, PacketCreatePlacingBlueprintPalette.class,
				Side.SERVER);
		network.reg(PacketOpenGui.HandlerServer.class, PacketOpenGui.class,
				Side.SERVER);

		network.reg(PacketBlueprintAddScheduleLayer.HandlerServer.class, PacketBlueprintAddScheduleLayer.class, Side.SERVER);
		network.reg(PacketBlueprintRemoveScheduleLayer.HandlerServer.class, PacketBlueprintRemoveScheduleLayer.class, Side.SERVER);
		network.reg(PacketBlueprintSetCurrentScheduleLayer.HandlerServer.class, PacketBlueprintSetCurrentScheduleLayer.class,
				Side.SERVER);
		network.reg(PacketAddNode.HandlerServer.class, PacketAddNode.class,
				Side.SERVER);
		network.reg(PacketSetScheduling.HandlerServer.class, PacketSetScheduling.class,
				Side.SERVER);
		network.reg(PacketAddSchedule.HandlerServer.class, PacketAddSchedule.class,
				Side.SERVER);
		network.reg(PacketRemoveSchedule.HandlerServer.class, PacketRemoveSchedule.class,
				Side.SERVER);
		network.reg(PacketSetScheduleTriggerId.HandlerServer.class, PacketSetScheduleTriggerId.class,
				Side.SERVER);
		network.reg(PacketSetScheduleLayerOptions.HandlerServer.class, PacketSetScheduleLayerOptions.class,
				Side.SERVER);
		network.reg(PacketTeleportOrbis.HandlerServer.class, PacketTeleportOrbis.class,
				Side.SERVER);

		network.reg(PacketTrackWorldAction.HandlerServer.class, PacketTrackWorldAction.class,
				Side.SERVER);
		network.reg(PacketRedoWorldAction.HandlerServer.class, PacketRedoWorldAction.class,
				Side.SERVER);
		network.reg(PacketUndoWorldAction.HandlerServer.class, PacketUndoWorldAction.class,
				Side.SERVER);
		network.reg(PacketClearWorldActions.HandlerServer.class, PacketClearWorldActions.class,
				Side.SERVER);

		network.reg(PacketRemoveEntrance.HandlerServer.class, PacketRemoveEntrance.class,
				Side.SERVER);

		network.reg(PacketSetFilterOptions.HandlerServer.class, PacketSetFilterOptions.class,
				Side.SERVER);

		network.reg(PacketRemoveNode.HandlerServer.class, PacketRemoveNode.class,
				Side.SERVER);

		network.reg(PacketBlueprintStackerGuiAddSlot.HandlerServer.class, PacketBlueprintStackerGuiAddSlot.class,
				Side.SERVER);
		network.reg(PacketBlueprintStackerGuiRemoveSlot.HandlerServer.class, PacketBlueprintStackerGuiRemoveSlot.class,
				Side.SERVER);
		network.reg(PacketBlueprintStackerGuiDisplaySlots.HandlerServer.class, PacketBlueprintStackerGuiDisplaySlots.class,
				Side.SERVER);

		network.reg(PacketPostGenAddLayer.HandlerServer.class, PacketPostGenAddLayer.class,
				Side.SERVER);
		network.reg(PacketPostGenRemoveLayer.HandlerServer.class, PacketPostGenRemoveLayer.class,
				Side.SERVER);
		network.reg(PacketPostGenDisplayLayers.HandlerServer.class, PacketPostGenDisplayLayers.class,
				Side.SERVER);

		network.reg(PacketBlueprintAddPostGenReplaceLayer.HandlerServer.class, PacketBlueprintAddPostGenReplaceLayer.class, Side.SERVER);
		network.reg(PacketBlueprintRemovePostGenReplaceLayer.HandlerServer.class, PacketBlueprintRemovePostGenReplaceLayer.class, Side.SERVER);
		network.reg(PacketBlueprintPostgenReplaceLayerChanges.HandlerServer.class, PacketBlueprintPostgenReplaceLayerChanges.class, Side.SERVER);

		network.reg(PacketBlueprintStackerInterface.HandlerServer.class, PacketBlueprintStackerInterface.class, Side.SERVER);

		network.reg(PacketBlueprintScheduleLayerGuiPos.HandlerServer.class, PacketBlueprintScheduleLayerGuiPos.class, Side.SERVER);

		network.reg(PacketSetEntranceTriggerId.HandlerServer.class, PacketSetEntranceTriggerId.class,
				Side.SERVER);

		// C L I E N T
		network.reg(PacketDeveloperMode.HandlerClient.class, PacketDeveloperMode.class, Side.CLIENT);
		network.reg(PacketWorldObjectManager.HandlerClient.class, PacketWorldObjectManager.class, Side.CLIENT);
		network.reg(PacketSendProject.HandlerClient.class, PacketSendProject.class, Side.CLIENT);
		network.reg(PacketSendProjectListing.HandlerClient.class, PacketSendProjectListing.class, Side.CLIENT);
		network.reg(PacketWorldObjectRemove.HandlerClient.class, PacketWorldObjectRemove.class, Side.CLIENT);
		network.reg(PacketWorldObjectAdd.HandlerClient.class, PacketWorldObjectAdd.class, Side.CLIENT);
		network.reg(PacketDeleteFile.HandlerClient.class, PacketDeleteFile.class, Side.CLIENT);
		network.reg(PacketChangePower.HandlerClient.class, PacketChangePower.class, Side.CLIENT);
		network.reg(PacketSetSelectedRegion.HandlerClient.class, PacketSetSelectedRegion.class, Side.CLIENT);
		network.reg(PacketSendDataCachePool.HandlerClient.class, PacketSendDataCachePool.class, Side.CLIENT);
		network.reg(PacketSendDataToCache.HandlerClient.class, PacketSendDataToCache.class, Side.CLIENT);
		network.reg(PacketStagedInventoryChanged.HandlerClient.class, PacketStagedInventoryChanged.class, Side.CLIENT);
		network.reg(PacketChangeSelectionInput.HandlerClient.class, PacketChangeSelectionInput.class, Side.CLIENT);
		network.reg(PacketChangeSelectionType.HandlerClient.class, PacketChangeSelectionType.class, Side.CLIENT);

		network.reg(PacketBlueprintAddScheduleLayer.HandlerClient.class, PacketBlueprintAddScheduleLayer.class, Side.CLIENT);
		network.reg(PacketBlueprintRemoveScheduleLayer.HandlerClient.class, PacketBlueprintRemoveScheduleLayer.class, Side.CLIENT);
		network.reg(PacketBlueprintSetCurrentScheduleLayer.HandlerClient.class, PacketBlueprintSetCurrentScheduleLayer.class,
				Side.CLIENT);
		network.reg(PacketAddNode.HandlerClient.class, PacketAddNode.class,
				Side.CLIENT);
		network.reg(PacketSetScheduling.HandlerClient.class, PacketSetScheduling.class,
				Side.CLIENT);
		network.reg(PacketAddSchedule.HandlerClient.class, PacketAddSchedule.class,
				Side.CLIENT);
		network.reg(PacketRemoveSchedule.HandlerClient.class, PacketRemoveSchedule.class,
				Side.CLIENT);
		network.reg(PacketSetScheduleTriggerId.HandlerClient.class, PacketSetScheduleTriggerId.class,
				Side.CLIENT);
		network.reg(PacketSetScheduleLayerOptions.HandlerClient.class, PacketSetScheduleLayerOptions.class,
				Side.CLIENT);

		network.reg(PacketRemoveEntrance.HandlerClient.class, PacketRemoveEntrance.class,
				Side.CLIENT);

		network.reg(PacketRemoveNode.HandlerClient.class, PacketRemoveNode.class,
				Side.CLIENT);

		network.reg(PacketBlueprintAddPostGenReplaceLayer.HandlerClient.class, PacketBlueprintAddPostGenReplaceLayer.class, Side.CLIENT);
		network.reg(PacketBlueprintRemovePostGenReplaceLayer.HandlerClient.class, PacketBlueprintRemovePostGenReplaceLayer.class, Side.CLIENT);
		network.reg(PacketBlueprintPostgenReplaceLayerChanges.HandlerClient.class, PacketBlueprintPostgenReplaceLayerChanges.class, Side.CLIENT);

		network.reg(PacketBlueprintScheduleLayerGuiPos.HandlerClient.class, PacketBlueprintScheduleLayerGuiPos.class, Side.CLIENT);

		network.reg(PacketSetEntranceTriggerId.HandlerClient.class, PacketSetEntranceTriggerId.class,
				Side.CLIENT);

		NetworkRegistry.INSTANCE.registerGuiHandler(OrbisCore.INSTANCE, new OrbisGuiHandler());
	}
}
