package com.gildedgames.orbis.client;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.util.BlockFilterHelper;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.client.gui.GuiChoiceMenuHolder;
import com.gildedgames.orbis.client.gui.GuiChoiceMenuPowers;
import com.gildedgames.orbis.client.gui.GuiChoiceMenuSelectionInputs;
import com.gildedgames.orbis.client.gui.GuiChoiceMenuSelectionTypes;
import com.gildedgames.orbis.client.renderers.AirSelectionRenderer;
import com.gildedgames.orbis.client.renderers.ChunkRendererManager;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.ItemsOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.*;
import com.gildedgames.orbis.common.player.godmode.GodPowerBlueprint;
import com.gildedgames.orbis.common.player.godmode.GodPowerSelect;
import com.gildedgames.orbis.common.player.godmode.selection_input.ISelectionInput;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber(Side.CLIENT)
public class OrbisDeveloperEventsClient
{

	public static final ChunkRendererManager CHUNK_RENDERER_MANAGER = new ChunkRendererManager();

	private static final Minecraft mc = Minecraft.getMinecraft();

	private static double prevReach;

	private static IWorldObject prevSelection;

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
		if (event.getGui() instanceof GuiIngameMenu)
		{
			final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);
			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			{
				if (selectionInput.getActiveSelection() != null && selectionInput.shouldClearSelectionOnEscape())
				{
					selectionInput.clearSelection();

					NetworkingOrbis.sendPacketToServer(new PacketClearSelection());

					event.setCanceled(true);
				}

				if (playerOrbis.powers().getSelectPower().getSelectedRegion() != null)
				{
					final WorldObjectManager manager = WorldObjectManager.get(mc.world);
					final IWorldObjectGroup group = manager.getGroup(0);

					NetworkingOrbis.sendPacketToServer(new PacketClearSelectedRegion());
					NetworkingOrbis.sendPacketToServer(new PacketWorldObjectRemove(mc.world, group, playerOrbis.powers().getSelectPower().getSelectedRegion()));

					playerOrbis.powers().getSelectPower().setSelectedRegion(null);

					event.setCanceled(true);
				}
			}
		}

		if (event.getGui() instanceof GuiInventory)
		{
			final Minecraft mc = Minecraft.getMinecraft();

			final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);

			if (playerOrbis.powers().getCurrentPower().hasCustomGui(playerOrbis))
			{
				playerOrbis.powers().getCurrentPower().onOpenGui(mc.player);
				playerOrbis.powers().getCurrentPower().getClientHandler().onOpenGui(mc.player);

				NetworkingOrbis.sendPacketToServer(new PacketOpenPowerGui());

				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onClienTick(final TickEvent.ClientTickEvent event)
	{
		if (mc.world != null && mc.player != null)
		{
			final PlayerOrbis playerOrbis = PlayerOrbis.get(mc.player);
			final GodPowerSelect select = playerOrbis.powers().getSelectPower();

			final ISelectionInput selectionInput = playerOrbis.selectionInputs().getCurrentSelectionInput();

			if (playerOrbis.inDeveloperMode())
			{
				final Minecraft mc = Minecraft.getMinecraft();

				final GuiScreen current = Minecraft.getMinecraft().currentScreen;

				final double reach = playerOrbis.getReach();

				if (OrbisKeyBindings.keyBindAlt.isPressed())
				{
					NetworkingOrbis.sendPacketToServer(new PacketSetScheduling(!playerOrbis.powers().isScheduling()));
					playerOrbis.powers().setScheduling(!playerOrbis.powers().isScheduling());
				}

				if (Keyboard.isKeyDown(OrbisKeyBindings.keyBindIncreaseReach.getKeyCode()))
				{
					playerOrbis.setDeveloperReach(reach + 1);
					NetworkingOrbis.sendPacketToServer(new PacketDeveloperReach(reach + 1));
				}

				if (Keyboard.isKeyDown(OrbisKeyBindings.keyBindDecreaseReach.getKeyCode()))
				{
					playerOrbis.setDeveloperReach(reach - 1);
					NetworkingOrbis.sendPacketToServer(new PacketDeveloperReach(reach - 1));
				}

				if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))
						&& OrbisKeyBindings.keyBindCopy.isPressed())
				{
					if (select.getSelectedRegion() != null)
					{
						final ItemStack item = new ItemStack(ItemsOrbis.block_chunk);

						NetworkingOrbis.sendPacketToServer(new PacketSetBlockDataContainerInHand(item, select.getSelectedRegion()));
						mc.player.inventory.setInventorySlotContents(mc.player.inventory.currentItem, item);
					}
				}

				if (OrbisKeyBindings.keyBindDelete.isPressed())
				{
					if (select.getSelectedRegion() != null)
					{
						final BlockFilter filter = new BlockFilter(BlockFilterHelper.getNewDeleteLayer(mc.player.getHeldItemMainhand()));

						NetworkingOrbis.sendPacketToServer(new PacketFilterShape(select.getSelectedRegion(), filter));
					}
				}

				if (OrbisKeyBindings.keyBindRotate.isPressed())
				{
					final GodPowerBlueprint power = playerOrbis.powers().getBlueprintPower();

					if (power.getPlacingBlueprint() != null)
					{
						power.setPlacingRotation(RotationHelp.getNextRotation(power.getPlacingRotation(), true));
						NetworkingOrbis.sendPacketToServer(new PacketRotateBlueprint());
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

				NetworkingOrbis.sendPacketToServer(new PacketDeveloperReach(prevReach));
			}
		}
		else
		{
			OrbisCore.stopProjectManager();
			OrbisCore.stopDataCache();
		}
	}

	@SubscribeEvent
	public static void onMouseEvent(final MouseEvent event)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

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

		if (playerOrbis.inDeveloperMode())
		{
			selectionInput.onMouseEvent(event, selector, playerOrbis);
		}

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

			final RayTraceResult blockRaytrace = RaytraceHelp
					.rayTraceNoBlocks(playerOrbis.getReach(), AirSelectionRenderer.PARTIAL_TICKS, playerOrbis.getEntity());
			double reach = playerOrbis.getReach();

			if (event.getDwheel() > 0)
			{
				playerOrbis.setDeveloperReach(reach + 1);
				NetworkingOrbis.sendPacketToServer(new PacketDeveloperReach(reach + 1));

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
					NetworkingOrbis.sendPacketToServer(new PacketDeveloperReach(distance));

					reach = playerOrbis.getReach();
				}

				playerOrbis.setDeveloperReach(reach - 1);
				NetworkingOrbis.sendPacketToServer(new PacketDeveloperReach(reach - 1));

				event.setCanceled(true);
			}
		}
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

		if (player != null && player.world != null)
		{
			final WorldObjectManager manager = WorldObjectManager.get(player.world);

			if (!manager.containsObserver(CHUNK_RENDERER_MANAGER))
			{
				manager.addObserver(CHUNK_RENDERER_MANAGER);
			}
		}
		else
		{
			OrbisCore.stopProjectManager();
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

	@SubscribeEvent
	public static void onRenderWorldLast(final RenderWorldLastEvent event)
	{
		final World world = Minecraft.getMinecraft().world;

		CHUNK_RENDERER_MANAGER.render(world, event.getPartialTicks());
	}

}
