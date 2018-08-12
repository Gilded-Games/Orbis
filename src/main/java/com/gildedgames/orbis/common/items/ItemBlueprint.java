package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlueprintRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketAddSchedule;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddBlueprint;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlueprint;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.DataCondition;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.management.IDataMetadata;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.data.schedules.ScheduleBlueprint;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
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

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBlueprint extends Item implements ModelRegisterCallback, ItemStackInput
{
	@SideOnly(Side.CLIENT)
	private static TileEntityBlueprintRenderer.BakedModel dummyModel;

	public ItemBlueprint()
	{
		super();

		this.setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":blueprint", "inventory"), dummyModel);
	}

	public static void setBlueprint(final ItemStack stack, final IDataIdentifier id)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		funnel.set("blueprint_id", id);
	}

	public static IDataIdentifier getBlueprintId(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("blueprint_id"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		final IDataIdentifier id = funnel.get("blueprint_id");

		return id;
	}

	public static BlueprintData getBlueprint(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("blueprint_id"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		final IDataIdentifier id = funnel.get("blueprint_id");

		return OrbisAPI.services().getProjectManager().findData(id);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":blueprint", "inventory"));

		final TileEntityBlueprintRenderer tesr = new TileEntityBlueprintRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlueprintRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityBlueprintRenderer.DummyTile.class);
	}

	@Override
	public String getItemStackDisplayName(final ItemStack stack)
	{
		final IDataIdentifier id = ItemBlueprint.getBlueprintId(stack);

		if (id != null)
		{
			try
			{
				final IDataMetadata data = OrbisCore.getProjectManager().findMetadata(id);

				return data.getName();
			}
			catch (OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisCore.LOGGER.error(e);
			}
		}

		return super.getItemStackDisplayName(stack);
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
	}

	@Override
	public void onUpdateInHand(final PlayerOrbis playerOrbis)
	{
		final World world = playerOrbis.getWorld();

		if (!world.isRemote)
		{
			return;
		}

		if (Minecraft.getMinecraft().currentScreen != null || !playerOrbis.getEntity().getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID()))
		{
			return;
		}

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && playerOrbis.powers().getBlueprintPower().getPlacingBlueprint() != null && playerOrbis.powers()
				.getCurrentPower()
				.canInteractWithItems(playerOrbis))
		{
            if(playerOrbis.getEntity().getCooldownTracker().hasCooldown(this))
                return;
            playerOrbis.getEntity().swingArm(EnumHand.MAIN_HAND);
            playerOrbis.getEntity().getCooldownTracker().setCooldown(this,4);
			final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());

			if (!pos.equals(playerOrbis.powers().getBlueprintPower().getPrevPlacingPos()))
			{
				playerOrbis.powers().getBlueprintPower().setPrevPlacingPos(pos);
				final BlockPos createPos = playerOrbis.raytraceNoSnapping();

				final Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();
				BlueprintData data = playerOrbis.powers().getBlueprintPower().getPlacingBlueprint();

				if (playerOrbis.powers().isScheduling())
				{
					BlueprintDataPalette palette = new BlueprintDataPalette();
					DataCondition condition = new DataCondition();

					palette.add(playerOrbis.powers().getBlueprintPower().getPlacingBlueprint(), condition);

					Region r = new Region(palette.getLargestDim());
					RegionHelp.translate(r, createPos);

					Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, r);

					if (b != null)
					{
						if (b.getCurrentScheduleLayerNode() != null)
						{
							r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());
							r.subtract(r.getWidth() / 2, 0, r.getLength() / 2);

							ScheduleBlueprint scheduleBlueprint = new ScheduleBlueprint("", palette, r);

							if (!Minecraft.getMinecraft().isIntegratedServerRunning())
							{
								OrbisCore.network()
										.sendPacketToDimension(new PacketAddSchedule(b, scheduleBlueprint, b.getCurrentScheduleLayerIndex()),
												world.provider.getDimension());
							}
							else
							{
								b.getCurrentScheduleLayerNode().getData().getScheduleRecord().addSchedule(scheduleBlueprint, b);
							}
						}
					}
					else
					{
						playerOrbis.getWorldActionLog().track(world, new WorldActionAddBlueprint(createPos));
					}
				}
				else
				{
					playerOrbis.getWorldActionLog()
							.track(world, new WorldActionBlueprint(data, createPos));
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
