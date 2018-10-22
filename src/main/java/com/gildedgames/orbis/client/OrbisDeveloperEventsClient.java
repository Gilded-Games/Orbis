package com.gildedgames.orbis.client;

import com.gildedgames.orbis.client.gui.GuiAlphaNotice;
import com.gildedgames.orbis.client.gui.GuiLayerEditor;
import com.gildedgames.orbis.client.gui.power_wheel.GuiChoiceMenuHolder;
import com.gildedgames.orbis.client.gui.power_wheel.GuiChoiceMenuPowers;
import com.gildedgames.orbis.client.gui.power_wheel.GuiChoiceMenuSelectionInputs;
import com.gildedgames.orbis.client.gui.power_wheel.GuiChoiceMenuSelectionTypes;
import com.gildedgames.orbis.client.model.ModelOrbisFloor;
import com.gildedgames.orbis.client.renderers.ChunkRendererManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.packets.*;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.player.godmode.GodPowerSelect;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionFilter;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.block.BlockFilter;
import com.gildedgames.orbis_api.block.BlockFilterHelper;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.util.RotationHelp;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber(Side.CLIENT)
public class OrbisDeveloperEventsClient
{

	public static final ChunkRendererManager CHUNK_RENDERER_MANAGER = new ChunkRendererManager();

	private static final Minecraft mc = Minecraft.getMinecraft();

	public static IBakedModel original;

	private static double prevReach;

	private static IWorldObject prevSelection;

	private static int prevDim;

	private static boolean prevDimSet;

	@SubscribeEvent
	public static void modelBakeEvent(final ModelBakeEvent event)
	{
		ModelResourceLocation resourceLocation = new ModelResourceLocation(new ResourceLocation("orbis", "orbis_floor"), "normal");
		original = event.getModelRegistry().getObject(resourceLocation);
		event.getModelRegistry().putObject(resourceLocation, new ModelOrbisFloor());
	}

	@SubscribeEvent()
	public static void onModelRegistryReady(final ModelRegistryEvent event)
	{
		for (final Item i : Item.REGISTRY)
		{
			if (i instanceof ModelRegisterCallback)
			{
				((ModelRegisterCallback) i).registerModel();
			}
		}
	}

	@SubscribeEvent
	public static void onGuiOpen(final GuiOpenEvent event)
	{
		if (event.getGui() instanceof GuiMainMenu && !OrbisCore.CONFIG.hasSeenAlphaNotice())
		{
			event.setGui(new GuiAlphaNotice());

			return;
		}

		if (event.getGui() instanceof GuiIngameMenu)
		{
			final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);
			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			{
				if (selectionInput.getActiveSelection() != null && selectionInput.shouldClearSelectionOnEscape())
				{
					selectionInput.clearSelection();

					OrbisCore.network().sendPacketToServer(new PacketClearSelection());

					event.setCanceled(true);
				}

				if (playerOrbis.powers().getSelectPower().getSelectedRegion() != null)
				{
					OrbisCore.network().sendPacketToServer(new PacketClearSelectedRegion());
					OrbisCore.network()
							.sendPacketToServer(new PacketWorldObjectRemove(mc.world, playerOrbis.powers().getSelectPower().getSelectedRegion()));

					playerOrbis.powers().getSelectPower().setSelectedRegion(null);

					event.setCanceled(true);
				}
			}
		}

		if (event.getGui() instanceof GuiInventory)
		{
			final Minecraft mc = Minecraft.getMinecraft();

			final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);

			final int x = MathHelper.floor(mc.player.posX);
			final int y = MathHelper.floor(mc.player.posY);
			final int z = MathHelper.floor(mc.player.posZ);

			Blueprint blueprint = WorldObjectUtils.getIntersectingShape(mc.player.world, Blueprint.class,
					new Region(new BlockPos(x, y, z), new BlockPos(x, MathHelper.floor(mc.player.posY + mc.player.height), z)));

			if (blueprint != null && OrbisKeyBindings.keyBindControl.isKeyDown())
			{
				Minecraft.getMinecraft().displayGuiScreen(new GuiLayerEditor(blueprint));

				event.setCanceled(true);
			}
			else if (playerOrbis.powers().getCurrentPower().hasCustomGui(playerOrbis))
			{
				playerOrbis.powers().getCurrentPower().onOpenGui(mc.player);
				playerOrbis.powers().getCurrentPower().getClientHandler().onOpenGui(mc.player);

				OrbisCore.network().sendPacketToServer(new PacketOpenPowerGui());

				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onClienTick(final TickEvent.ClientTickEvent event)
	{
		final Minecraft mc = FMLClientHandler.instance().getClient();

		final World world = FMLClientHandler.instance().getWorldClient();
		final EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();

		if (mc.world != null && mc.player != null)
		{
			final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);
			final GodPowerSelect select = playerOrbis.powers().getSelectPower();

			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			if (playerOrbis.inDeveloperMode())
			{
				final GuiScreen current = Minecraft.getMinecraft().currentScreen;

				final double reach = playerOrbis.getReach();

				/*if (OrbisKeyBindings.keyBindAlt.isPressed())
				{
					OrbisCore.network().sendPacketToServer(new PacketSetScheduling(!playerOrbis.powers().isScheduling()));
					playerOrbis.powers().setScheduling(!playerOrbis.powers().isScheduling());
				}*/

				if (OrbisKeyBindings.keyBindIncreaseReach.isPressed() || (OrbisKeyBindings.keyBindIncreaseReach.isKeyDown() && Keyboard.isRepeatEvent()))
				{
					playerOrbis.setDeveloperReach(reach + 1);
				}

				if (OrbisKeyBindings.keyBindDecreaseReach.isPressed() || (OrbisKeyBindings.keyBindDecreaseReach.isKeyDown() && Keyboard.isRepeatEvent()))
				{
					playerOrbis.setDeveloperReach(reach - 1);
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
				{
					String worldActionLog = playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getCreativePower() ?
							WorldActionLogs.BLOCKS : WorldActionLogs.NORMAL;

					if (OrbisKeyBindings.keyBindRedo.isPressed())
					{
						playerOrbis.getWorldActionLog(worldActionLog).redo(mc.world);
					}
					else if (OrbisKeyBindings.keyBindUndo.isPressed())
					{
						playerOrbis.getWorldActionLog(worldActionLog).undo(mc.world);
					}
				}

				if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
						&& OrbisKeyBindings.keyBindCopy.isPressed())
				{
					if (select.getSelectedRegion() != null)
					{
						final ItemStack item = new ItemStack(ItemsOrbis.block_chunk);

						OrbisCore.network().sendPacketToServer(new PacketSetBlockDataContainerInHand(item, select.getSelectedRegion()));
						mc.player.inventory.setInventorySlotContents(mc.player.inventory.currentItem, item);
					}
				}

				if (OrbisKeyBindings.keyBindDelete.isPressed())
				{
					if (select.getSelectedRegion() != null)
					{
						final BlockFilter filter = new BlockFilter(BlockFilterHelper.getNewDeleteLayer(mc.player.getHeldItemMainhand()));

						playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL)
								.apply(mc.world, new WorldActionFilter(select.getSelectedRegion().getBoundingBox(), filter, false));
					}
				}

				if (OrbisKeyBindings.keyBindRotate.isPressed())
				{
					final GodPowerBlueprint power = playerOrbis.powers().getBlueprintPower();

					if (power.getPlacingBlueprint() != null || power.getPlacingPalette() != null)
					{
						power.setPlacingRotation(RotationHelp.getNextRotation(power.getPlacingRotation(), true));
						OrbisCore.network().sendPacketToServer(new PacketRotateBlueprint());
					}
				}

				if (Keyboard.isKeyDown(OrbisKeyBindings.keyBindFindPower.getKeyCode()))
				{
					if (current == null)
					{
						final GuiChoiceMenuHolder choiceMenuHolder = new GuiChoiceMenuHolder(new GuiChoiceMenuPowers(playerOrbis),
								new GuiChoiceMenuSelectionTypes(playerOrbis), new GuiChoiceMenuSelectionInputs(playerOrbis));

						Minecraft.getMinecraft().displayGuiScreen(choiceMenuHolder);
					}
				}
				else if (current instanceof GuiChoiceMenuHolder)
				{
					final GuiChoiceMenuHolder menu = (GuiChoiceMenuHolder) current;

					if (menu.getCurrentMenu().getHoveredChoice() != null)
					{
						menu.getCurrentMenu().getHoveredChoice().onSelect(playerOrbis);
					}

					Minecraft.getMinecraft().displayGuiScreen(null);
				}
			}

			if (selectionInput.getActiveSelection() == null && prevSelection != null && prevReach != 0.0D)
			{
				prevSelection = null;
				playerOrbis.setDeveloperReach(prevReach);
			}
		}
		else if (player == null && world == null && (!mc.isIntegratedServerRunning() || mc.getIntegratedServer() == null))
		{
			OrbisAPI.services().stopProjectManager();
			OrbisCore.stopDataCache();
		}
	}

	@SubscribeEvent
	public static void onMouseEvent(final MouseEvent event)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.inDeveloperMode())
		{
			IShapeSelector selector = playerOrbis.powers().getCurrentPower().getShapeSelector();
			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

			if (held.getItem() instanceof IShapeSelector)
			{
				selector = (IShapeSelector) held.getItem();
			}

			if (held.getItem() instanceof ItemStackInput)
			{
				final ItemStackInput input = (ItemStackInput) held.getItem();

				input.onMouseEvent(event, playerOrbis);
			}

			selectionInput.onMouseEvent(event, selector, playerOrbis);

			final IWorldObject activeRegion = selectionInput.getActiveSelection();

			//Change reach
			if (OrbisKeyBindings.keyBindControl.isKeyDown() || (activeRegion != null && selectionInput.shouldClearSelectionOnEscape()))
			{
				if (activeRegion != null && prevSelection == null)
				{
					prevSelection = activeRegion;

					prevReach = playerOrbis.getDeveloperReach();
				}

				if (OrbisKeyBindings.keyBindControl.isKeyDown())
				{
					prevReach = playerOrbis.getDeveloperReach();
				}

				if (playerOrbis.powers().getCurrentPower() != playerOrbis.powers().getCreativePower())
				{
					final RayTraceResult blockRaytrace = OrbisRaytraceHelp.getStandardRaytrace(playerOrbis.getEntity());
					double reach = playerOrbis.getReach();

					if (event.getDwheel() > 0)
					{
						playerOrbis.setDeveloperReach(reach + 1);

						event.setCanceled(true);
					}
					else if (event.getDwheel() < 0)
					{
						if (blockRaytrace != null)
						{
							final int x = MathHelper.floor(playerOrbis.getEntity().posX);
							final int y = MathHelper.floor(playerOrbis.getEntity().posY);
							final int z = MathHelper.floor(playerOrbis.getEntity().posZ);

							final double deltaX = x - blockRaytrace.hitVec.x;
							final double deltaY = y - blockRaytrace.hitVec.y;
							final double deltaZ = z - blockRaytrace.hitVec.z;

							final float distance = MathHelper.floor((float) Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ));

							playerOrbis.setDeveloperReach(distance);

							reach = playerOrbis.getReach();
						}

						playerOrbis.setDeveloperReach(reach - 1);

						event.setCanceled(true);
					}
				}
			}
		}
	}

	private static void onClientChangeDimension()
	{
		CHUNK_RENDERER_MANAGER.unload();
		WorldObjectManager manager = WorldObjectManager.get(mc.player.world);

		CHUNK_RENDERER_MANAGER.onReloaded(manager);
	}

	@SubscribeEvent
	public static void onClientTick(final TickEvent.ClientTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
		{
			return;
		}

		final Minecraft mc = FMLClientHandler.instance().getClient();

		final World world = FMLClientHandler.instance().getWorldClient();

		final EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();

		if (player != null)
		{
			if (!prevDimSet)
			{
				prevDim = player.dimension;
				prevDimSet = true;
			}

			if (player.dimension != prevDim)
			{
				prevDim = player.dimension;

				onClientChangeDimension();
			}
		}

		if (player != null && player.world != null)
		{
			final WorldObjectManager manager = WorldObjectManager.get(player.world);

			if (!manager.containsObserver(CHUNK_RENDERER_MANAGER))
			{
				manager.addObserver(CHUNK_RENDERER_MANAGER);
			}
		}
		else if (player == null && world == null && (!mc.isIntegratedServerRunning() || mc.getIntegratedServer() == null))
		{
			OrbisAPI.services().stopProjectManager();
			CHUNK_RENDERER_MANAGER.unload();
		}

		if (world != null && player != null)
		{
			final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis != null)
			{
				if (!playerOrbis.containsObserver(CHUNK_RENDERER_MANAGER))
				{
					playerOrbis.addObserver(CHUNK_RENDERER_MANAGER);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRenderWorldLast(final RenderWorldLastEvent event)
	{
		ModelOrbisFloor.setMimicModel(null);
		ModelOrbisFloor.setFloorColor(0xFFFFFF);

		final World world = Minecraft.getMinecraft().world;

		CHUNK_RENDERER_MANAGER.render(world, event.getPartialTicks());
	}

}
