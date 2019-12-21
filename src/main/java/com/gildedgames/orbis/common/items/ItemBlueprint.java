package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlueprintRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketAddSchedule;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketGenerateBlueprintNetwork;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddBlueprint;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlueprint;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.OrbisLib;
import com.gildedgames.orbis.lib.data.DataCondition;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.data.management.IDataMetadata;
import com.gildedgames.orbis.lib.data.management.IProject;
import com.gildedgames.orbis.lib.data.region.Region;
import com.gildedgames.orbis.lib.data.schedules.ISchedule;
import com.gildedgames.orbis.lib.data.schedules.ScheduleBlueprint;
import com.gildedgames.orbis.lib.data.schedules.ScheduleEntranceHolder;
import com.gildedgames.orbis.lib.util.RotationHelp;
import com.gildedgames.orbis.lib.util.io.NBTFunnel;
import com.gildedgames.orbis.lib.world.WorldObjectUtils;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Optional;

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

	public static OrbisItemMetadata getOrbisMetadata(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("metadata"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		return funnel.get("metadata");
	}

	public static void setBlueprint(final ItemStack stack, final IDataIdentifier id)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		funnel.set("blueprint_id", id);

		Optional<BlueprintData> data = OrbisLib.services().getProjectManager().findData(id);

		data.ifPresent(blueprintData -> funnel.set("metadata", new OrbisItemMetadata(blueprintData.getMetadata().getName(), new Region(blueprintData))));
	}

	public static IDataIdentifier getBlueprintId(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("blueprint_id"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		return funnel.get("blueprint_id");
	}

	public static Optional<BlueprintData> getBlueprint(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("blueprint_id"))
		{
			return Optional.empty();
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		final IDataIdentifier id = funnel.get("blueprint_id");

		return OrbisLib.services().getProjectManager().findData(id);
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
			final Optional<IDataMetadata> data = OrbisCore.getProjectManager().findMetadata(id);

			if (data.isPresent())
			{
				return data.get().getName();
			}
			else
			{
				OrbisItemMetadata meta = getOrbisMetadata(stack);

				if (meta != null)
				{
					if (OrbisCore.getProjectManager().projectExists(id.getProjectIdentifier()))
					{
						Optional<IProject> project = OrbisCore.getProjectManager().findProject(id.getProjectIdentifier());

						if (project.isPresent())
						{
							if (project.get().getInfo().getMetadata().isDownloaded() || Minecraft.getMinecraft().isIntegratedServerRunning())
							{
								return meta.getName() + " (Data Missing)";
							}
							else
							{
								return meta.getName() + " (Project Not Downloaded)";
							}
						}
					}
					else
					{
						return meta.getName() + " (Project Missing)";
					}
				}
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

		if (Minecraft.getMinecraft().currentScreen != null)
		{
			return;
		}

		if (!playerOrbis.inDeveloperMode())
		{
			return;
		}

		BlueprintData data = playerOrbis.powers().getBlueprintPower().getPlacingBlueprint();

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && data != null && playerOrbis.powers()
				.getCurrentPower()
				.canInteractWithItems(playerOrbis))
		{
			if (playerOrbis.getEntity().getCooldownTracker().hasCooldown(this))
			{
				return;
			}

			playerOrbis.getEntity().swingArm(EnumHand.MAIN_HAND);
			playerOrbis.getEntity().getCooldownTracker().setCooldown(this, 4);

			final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());

			if (!pos.equals(playerOrbis.powers().getBlueprintPower().getPrevPlacingPos()))
			{
				playerOrbis.powers().getBlueprintPower().setPrevPlacingPos(pos);

				if (Keyboard.isKeyDown(Keyboard.KEY_LMENU))
				{
					this.generateNetwork(playerOrbis, data);
				}
				else
				{
					this.generateBlueprint(playerOrbis, world, data);
				}
			}
		}
	}

	private void generateNetwork(PlayerOrbis playerOrbis, BlueprintData data)
	{
		final BlockPos createPos = playerOrbis.raytraceNoSnapping();

		OrbisCore.network()
				.sendPacketToServer(new PacketGenerateBlueprintNetwork(data.getMetadata().getIdentifier(), createPos));
	}

	private void generateBlueprint(PlayerOrbis playerOrbis, World world, BlueprintData data)
	{
		final BlockPos createPos = playerOrbis.raytraceNoSnapping();
		final Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();

		if (playerOrbis.powers().isScheduling() || playerOrbis.powers().isEntrance())
		{
			Region scheduleBounds = new Region(RotationHelp.regionFromCenter(createPos, data, rotation));
			Blueprint b = WorldObjectUtils.getIntersectingShape(world, Blueprint.class, scheduleBounds);

			if (b != null)
			{
				if (b.getCurrentScheduleLayerNode() != null)
				{
					scheduleBounds.subtract(b.getPos().getX(), b.getPos().getY(), b.getPos().getZ());

					boolean shouldAddEntrance = playerOrbis.powers().isEntrance() && data.getEntrance() != null;
					ISchedule schedule;

					if (shouldAddEntrance)
					{
						schedule = new ScheduleEntranceHolder("", data.getMetadata().getIdentifier(), scheduleBounds, rotation);
					}
					else
					{
						BlueprintDataPalette palette = new BlueprintDataPalette();
						palette.add(data, new DataCondition());

						schedule = new ScheduleBlueprint("", palette, scheduleBounds, rotation);
					}

					if (!Minecraft.getMinecraft().isIntegratedServerRunning())
					{
						OrbisCore.network()
								.sendPacketToDimension(new PacketAddSchedule(b, schedule, b.getCurrentScheduleLayerIndex()),
										world.provider.getDimension());
					}
					else
					{
						b.getCurrentScheduleLayerNode().getData().getScheduleRecord().addSchedule(schedule, b);
					}
				}
			}
			else
			{
				playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL).apply(world, new WorldActionAddBlueprint(createPos));
			}
		}
		else
		{
			playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL)
					.apply(world, new WorldActionBlueprint(data, createPos));
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
