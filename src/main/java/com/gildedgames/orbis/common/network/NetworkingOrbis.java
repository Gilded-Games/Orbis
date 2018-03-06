package com.gildedgames.orbis.common.network;

import com.gildedgames.orbis.api.packets.instances.INetworkOrbis;
import com.gildedgames.orbis.api.packets.instances.PacketRegisterDimension;
import com.gildedgames.orbis.api.packets.instances.PacketRegisterInstance;
import com.gildedgames.orbis.api.packets.instances.PacketUnregisterDimension;
import com.gildedgames.orbis.api.packets.util.IMessageMultipleParts;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.network.packets.*;
import com.gildedgames.orbis.common.network.packets.blueprints.*;
import com.gildedgames.orbis.common.network.packets.framework.PacketAddNode;
import com.gildedgames.orbis.common.network.packets.projects.*;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketClearWorldActions;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketRedoWorldAction;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketTrackWorldAction;
import com.gildedgames.orbis.common.network.packets.world_actions.PacketUndoWorldAction;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetworkingOrbis implements INetworkOrbis
{
	private static final HashMap<Integer, ArrayList<byte[]>> packetParts = Maps.newHashMap();

	private static SimpleNetworkWrapper instance;

	private static int discriminant;

	public static void preInit()
	{
		instance = NetworkRegistry.INSTANCE.newSimpleChannel(OrbisCore.MOD_ID);

		// S E R V E R
		instance.registerMessage(PacketDeveloperReach.HandlerServer.class, PacketDeveloperReach.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketWorldObjectAdd.HandlerServer.class, PacketWorldObjectAdd.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketWorldObjectRemove.HandlerServer.class, PacketWorldObjectRemove.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketActiveSelection.HandlerServer.class, PacketActiveSelection.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketChangePower.HandlerServer.class, PacketChangePower.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketOpenPowerGui.HandlerServer.class, PacketOpenPowerGui.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketSetItemStack.HandlerServer.class, PacketSetItemStack.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketChangeSelectionType.HandlerServer.class, PacketChangeSelectionType.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketFilterShape.HandlerServer.class, PacketFilterShape.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketRequestProject.HandlerServer.class, PacketRequestProject.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketRequestProjectListing.HandlerServer.class, PacketRequestProjectListing.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketSaveWorldObjectToProject.HandlerServer.class, PacketSaveWorldObjectToProject.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketRequestCreateProject.HandlerServer.class, PacketRequestCreateProject.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketDeleteFile.HandlerServer.class, PacketDeleteFile.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketClearSelection.HandlerServer.class, PacketClearSelection.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketRotateBlueprint.HandlerServer.class, PacketRotateBlueprint.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketClearSelectedRegion.HandlerServer.class, PacketClearSelectedRegion.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketSetItemStackInHand.HandlerServer.class, PacketSetItemStackInHand.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketSetBlockDataContainerInHand.HandlerServer.class, PacketSetBlockDataContainerInHand.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketChangeSelectionInput.HandlerServer.class, PacketChangeSelectionInput.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketCreatePlacingBlueprint.HandlerServer.class, PacketCreatePlacingBlueprint.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketCreateItemBlockDataContainer.HandlerServer.class, PacketCreateItemBlockDataContainer.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketCreatePlacingBlueprintPalette.HandlerServer.class, PacketCreatePlacingBlueprintPalette.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketOpenGui.HandlerServer.class, PacketOpenGui.class, discriminant++,
				Side.SERVER);

		instance.registerMessage(PacketBlueprintAddScheduleLayer.HandlerServer.class, PacketBlueprintAddScheduleLayer.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketBlueprintRemoveScheduleLayer.HandlerServer.class, PacketBlueprintRemoveScheduleLayer.class, discriminant++, Side.SERVER);
		instance.registerMessage(PacketBlueprintSetCurrentScheduleLayer.HandlerServer.class, PacketBlueprintSetCurrentScheduleLayer.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketAddNode.HandlerServer.class, PacketAddNode.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketSetScheduling.HandlerServer.class, PacketSetScheduling.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketAddSchedule.HandlerServer.class, PacketAddSchedule.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketRemoveSchedule.HandlerServer.class, PacketRemoveSchedule.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketSetTriggerId.HandlerServer.class, PacketSetTriggerId.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketSetScheduleLayerInfo.HandlerServer.class, PacketSetScheduleLayerInfo.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketTeleportOrbis.HandlerServer.class, PacketTeleportOrbis.class, discriminant++,
				Side.SERVER);

		instance.registerMessage(PacketTrackWorldAction.HandlerServer.class, PacketTrackWorldAction.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketRedoWorldAction.HandlerServer.class, PacketRedoWorldAction.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketUndoWorldAction.HandlerServer.class, PacketUndoWorldAction.class, discriminant++,
				Side.SERVER);
		instance.registerMessage(PacketClearWorldActions.HandlerServer.class, PacketClearWorldActions.class, discriminant++,
				Side.SERVER);

		instance.registerMessage(PacketRemoveEntrance.HandlerServer.class, PacketRemoveEntrance.class, discriminant++,
				Side.SERVER);

		// C L I E N T
		instance.registerMessage(PacketDeveloperMode.HandlerClient.class, PacketDeveloperMode.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketWorldObjectManager.HandlerClient.class, PacketWorldObjectManager.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketDeveloperReach.HandlerClient.class, PacketDeveloperReach.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketSendProject.HandlerClient.class, PacketSendProject.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketSendProjectListing.HandlerClient.class, PacketSendProjectListing.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketWorldObjectRemove.HandlerClient.class, PacketWorldObjectRemove.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketWorldObjectAdd.HandlerClient.class, PacketWorldObjectAdd.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketDeleteFile.HandlerClient.class, PacketDeleteFile.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketChangePower.HandlerClient.class, PacketChangePower.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketSetSelectedRegion.HandlerClient.class, PacketSetSelectedRegion.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketSendDataCachePool.HandlerClient.class, PacketSendDataCachePool.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketSendDataToCache.HandlerClient.class, PacketSendDataToCache.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketStagedInventoryChanged.HandlerClient.class, PacketStagedInventoryChanged.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketChangeSelectionInput.HandlerClient.class, PacketChangeSelectionInput.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketChangeSelectionType.HandlerClient.class, PacketChangeSelectionType.class, discriminant++, Side.CLIENT);

		instance.registerMessage(PacketBlueprintAddScheduleLayer.HandlerClient.class, PacketBlueprintAddScheduleLayer.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketBlueprintRemoveScheduleLayer.HandlerClient.class, PacketBlueprintRemoveScheduleLayer.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketBlueprintSetCurrentScheduleLayer.HandlerClient.class, PacketBlueprintSetCurrentScheduleLayer.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketAddNode.HandlerClient.class, PacketAddNode.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketSetScheduling.HandlerClient.class, PacketSetScheduling.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketAddSchedule.HandlerClient.class, PacketAddSchedule.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketRemoveSchedule.HandlerClient.class, PacketRemoveSchedule.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketSetTriggerId.HandlerClient.class, PacketSetTriggerId.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketSetScheduleLayerInfo.HandlerClient.class, PacketSetScheduleLayerInfo.class, discriminant++,
				Side.CLIENT);
		instance.registerMessage(PacketRegisterDimension.Handler.class, PacketRegisterDimension.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketUnregisterDimension.Handler.class, PacketUnregisterDimension.class, discriminant++, Side.CLIENT);
		instance.registerMessage(PacketRegisterInstance.Handler.class, PacketRegisterInstance.class, discriminant++, Side.CLIENT);

		instance.registerMessage(PacketRemoveEntrance.HandlerClient.class, PacketRemoveEntrance.class, discriminant++,
				Side.CLIENT);

		NetworkRegistry.INSTANCE.registerGuiHandler(OrbisCore.INSTANCE, new OrbisGuiHandler());
	}

	@Override
	public Map<Integer, ArrayList<byte[]>> getPacketParts()
	{
		return NetworkingOrbis.packetParts;
	}

	private IMessage[] fetchParts(final IMessage message)
	{
		final IMessage[] parts;

		if (message instanceof IMessageMultipleParts)
		{
			final IMessageMultipleParts multipleParts = (IMessageMultipleParts) message;
			parts = multipleParts.getParts();
		}
		else
		{
			parts = new IMessage[1];

			parts[0] = message;
		}

		return parts;
	}

	@Override
	public void sendPacketToDimension(final IMessage message, final int dimension)
	{
		for (final IMessage part : this.fetchParts(message))
		{
			NetworkingOrbis.instance.sendToDimension(part, dimension);
		}
	}

	@Override
	public void sendPacketToAllPlayers(final IMessage message)
	{
		for (final IMessage part : this.fetchParts(message))
		{
			NetworkingOrbis.instance.sendToAll(part);
		}
	}

	@Override
	public void sendPacketToAllPlayersExcept(final IMessage message, final EntityPlayerMP player)
	{
		final PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

		for (final IMessage part : this.fetchParts(message))
		{
			for (final EntityPlayerMP p : playerList.getPlayers())
			{
				if (p != player)
				{
					NetworkingOrbis.instance.sendTo(part, p);
				}
			}
		}
	}

	@Override
	public void sendPacketToPlayer(final IMessage message, final EntityPlayerMP player)
	{
		for (final IMessage part : this.fetchParts(message))
		{
			NetworkingOrbis.instance.sendTo(part, player);
		}
	}

	@Override
	public void sendPacketToWatching(final IMessage message, final EntityLivingBase entity, final boolean includeSelf)
	{
		for (final IMessage part : this.fetchParts(message))
		{
			final WorldServer world = (WorldServer) entity.world;

			final EntityTracker tracker = world.getEntityTracker();

			for (final EntityPlayer player : tracker.getTrackingPlayers(entity))
			{
				this.sendPacketToPlayer(part, (EntityPlayerMP) player);
			}

			// Entities don't watch themselves, take special care here
			if (includeSelf && entity instanceof EntityPlayer)
			{
				this.sendPacketToPlayer(part, (EntityPlayerMP) entity);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void sendPacketToServer(final IMessage message)
	{
		for (final IMessage part : this.fetchParts(message))
		{
			NetworkingOrbis.instance.sendToServer(part);
		}
	}
}
