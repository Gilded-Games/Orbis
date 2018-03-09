package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.schedules.ScheduleBlueprint;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.WorldObjectUtils;
import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlueprintPaletteRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.OrbisServerCaches;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketAddSchedule;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlueprintPalette;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.util.concurrent.ExecutionException;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBlueprintPalette extends Item implements ModelRegisterCallback, ItemStackInput
{
	@SideOnly(Side.CLIENT)
	private static TileEntityBlueprintPaletteRenderer.BakedModel dummyModel;

	public ItemBlueprintPalette()
	{
		super();

		this.setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":blueprint_palette", "inventory"), dummyModel);
	}

	public static void setBlueprintPalette(final ItemStack stack, final BlueprintDataPalette palette)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		funnel.set("palette", palette);
	}

	public static BlueprintDataPalette getBlueprintPalette(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("palette"))
		{
			return null;
		}

		try
		{
			return OrbisServerCaches.getBlueprintPalettes().get(stack.getTagCompound()).orNull();
		}
		catch (final ExecutionException e)
		{
			OrbisCore.LOGGER.error(e);
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":blueprint_palette", "inventory"));

		final TileEntityBlueprintPaletteRenderer tesr = new TileEntityBlueprintPaletteRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlueprintPaletteRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityBlueprintPaletteRenderer.DummyTile.class);
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	@Override
	public void onUpdateInHand(final PlayerOrbis playerOrbis)
	{
		final World world = playerOrbis.getWorld();

		if (!world.isRemote)
		{
			return;
		}

		if (Minecraft.getMinecraft().currentScreen != null)
		{
			return;
		}

		BlueprintDataPalette palette = playerOrbis.powers().getBlueprintPower().getPlacingPalette();

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && palette != null && playerOrbis.powers().getCurrentPower()
				.canInteractWithItems(playerOrbis))
		{
			final BlockPos pos = RaytraceHelp.doOrbisRaytrace(playerOrbis, playerOrbis.raytraceWithRegionSnapping());

			if (!pos.equals(playerOrbis.powers().getBlueprintPower().getPrevPlacingPos()))
			{
				playerOrbis.powers().getBlueprintPower().setPrevPlacingPos(pos);
				final BlockPos createPos = playerOrbis.raytraceNoSnapping();

				if (playerOrbis.powers().isScheduling())
				{
					Region r = new Region(palette.getLargestDim());
					RegionHelp.translate(r, createPos);

					Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, r);

					if (b != null)
					{
						r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());
						r.subtract(r.getWidth() / 2, 0, r.getLength() / 2);

						ScheduleBlueprint scheduleBlueprint = new ScheduleBlueprint("", palette, r);

						if (!Minecraft.getMinecraft().isIntegratedServerRunning())
						{
							OrbisAPI.network()
									.sendPacketToDimension(new PacketAddSchedule(b, scheduleBlueprint, b.getCurrentScheduleLayerIndex()),
											world.provider.getDimension());
						}
						else
						{
							b.getCurrentScheduleLayer().getScheduleRecord().addSchedule(scheduleBlueprint);
						}
					}
				}
				else
				{
					playerOrbis.getWorldActionLog().track(world, new WorldActionBlueprintPalette(createPos));
				}
			}
		}
	}

	@Override
	public void onMouseEvent(final MouseEvent event, final PlayerOrbis playerOrbis)
	{
		if (event.getButton() == 0 || event.getButton() == 1)
		{
			event.setCanceled(true);
		}
	}
}
