package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlueprintStackerRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlueprintStacker;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.data.blueprint.BlueprintStackerData;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
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

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBlueprintStacker extends Item implements ModelRegisterCallback, ItemStackInput
{
	@SideOnly(Side.CLIENT)
	private static TileEntityBlueprintStackerRenderer.BakedModel dummyModel;

	public ItemBlueprintStacker()
	{
		super();

		this.setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":blueprint_stacker", "inventory"), dummyModel);
	}

	public static void setBlueprintStacker(final ItemStack stack, final BlueprintStackerData stacker)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		funnel.set("stacker_id", stacker.getMetadata().getIdentifier());
	}

	public static IDataIdentifier getBlueprintStackerId(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("stacker_id"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		return funnel.get("stacker_id");
	}

	public static BlueprintStackerData getBlueprintStacker(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("stacker_id"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		final IDataIdentifier id = funnel.get("stacker_id");

		return OrbisAPI.services().getProjectManager().findData(id);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":blueprint_stacker", "inventory"));

		final TileEntityBlueprintStackerRenderer tesr = new TileEntityBlueprintStackerRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlueprintStackerRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityBlueprintStackerRenderer.DummyTile.class);
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

		if (Minecraft.getMinecraft().currentScreen != null || !playerOrbis.getEntity().getUniqueID().equals(Minecraft.getMinecraft().player.getUniqueID()))
		{
			return;
		}

		BlueprintStackerData stacker = playerOrbis.powers().getBlueprintPower().getStackerInHand();

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && stacker != null && playerOrbis.powers().getCurrentPower()
				.canInteractWithItems(playerOrbis))
		{
			final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());

			if (!pos.equals(playerOrbis.powers().getBlueprintPower().getPrevPlacingPos()))
			{
				playerOrbis.powers().getBlueprintPower().setPrevPlacingPos(pos);
				final BlockPos createPos = playerOrbis.raytraceNoSnapping();

				if (playerOrbis.powers().isScheduling())
				{
					/*Region r = new Region(stacker.getLargestDim());
					RegionHelp.translate(r, createPos);

					Blueprint b = WorldObjectUtils.locate(world, Blueprint.class, r);

					if (b != null)
					{
						r.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());
						r.subtract(r.getWidth() / 2, 0, r.getLength() / 2);

						ScheduleBlueprint scheduleBlueprint = new ScheduleBlueprint("", stacker, r);

						if (!Minecraft.getMinecraft().isIntegratedServerRunning())
						{
							OrbisCore.network()
									.sendPacketToDimension(new PacketAddSchedule(b, scheduleBlueprint, b.getCurrentScheduleLayerIndex()),
											world.provider.getDimension());
						}
						else
						{
							b.getCurrentScheduleLayer().getScheduleRecord().addSchedule(scheduleBlueprint);
						}
					}*/
				}
				else
				{
					playerOrbis.getWorldActionLog().track(world, new WorldActionBlueprintStacker(createPos));
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
