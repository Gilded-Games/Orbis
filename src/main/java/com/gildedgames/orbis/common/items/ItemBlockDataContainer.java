package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlockDataContainerRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.network.packets.PacketSendDataToCache;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionAddWorldObject;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlockDataContainer;
import com.gildedgames.orbis.common.world_objects.GhostBlockDataContainer;
import com.gildedgames.orbis_api.block.BlockDataContainer;
import com.gildedgames.orbis_api.data.management.IDataCache;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.util.RotationHelp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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

import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBlockDataContainer extends Item implements ModelRegisterCallback, ItemStackInput
{

	@SideOnly(Side.CLIENT)
	private static TileEntityBlockDataContainerRenderer.BakedModel dummyModel;

	public ItemBlockDataContainer()
	{
		super();

		this.setHasSubtypes(true);
	}

	public static void setDataContainer(final EntityPlayer player, final ItemStack stack, final BlockDataContainer container)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final Optional<IDataCache> cache = OrbisCore.getDataCache().findCache(OrbisCore.BLOCK_DATA_CONTAINERS_CACHE);

		if (cache.isPresent())
		{
			final boolean shouldSend = container.getMetadata().getIdentifier() == null;
			final UUID id = cache.get().addData(container);

			stack.getTagCompound().setUniqueId("dataId", id);

			if (!player.world.isRemote && shouldSend)
			{
				OrbisCore.network().sendPacketToAllPlayers(new PacketSendDataToCache(OrbisCore.BLOCK_DATA_CONTAINERS_CACHE, container));
			}
		}
	}

	public static Optional<BlockDataContainer> getDataContainer(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("dataIdLeast"))
		{
			return Optional.empty();
		}

		final NBTTagCompound tag = stack.getTagCompound();

		final Optional<IDataCache> cache = OrbisCore.getDataCache().findCache(OrbisCore.BLOCK_DATA_CONTAINERS_CACHE);

		if (cache.isPresent())
		{
			return cache.get().getData(tag.getUniqueId("dataId"));
		}

		return Optional.empty();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":block_chunk", "inventory"), dummyModel);
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

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1))
				&& playerOrbis.powers().getBlueprintPower().getPlacingBlueprint() != null)
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
				final BlockPos createPos = playerOrbis.raytraceNoSnapping();

				if (playerOrbis.getCreationSettings().placeChunksAsGhostRegions())
				{
					final Optional<BlockDataContainer> container = ItemBlockDataContainer.getDataContainer(playerOrbis.getEntity().getHeldItemMainhand());

					if (container.isPresent())
					{
						Rotation rotation = playerOrbis.powers().getBlueprintPower().getPlacingRotation();

						Region tempRegion = new Region(container.get());

						IRegion tempRegion2 = RotationHelp.regionFromCenter(createPos, tempRegion, rotation);

						GhostBlockDataContainer ghost = new GhostBlockDataContainer(world, tempRegion2.getMin(), container.get());

						playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL).apply(world, new WorldActionAddWorldObject(ghost));
					}
				}
				else
				{
					playerOrbis.getWorldActionLog(WorldActionLogs.NORMAL)
							.apply(world, new WorldActionBlockDataContainer(playerOrbis.getEntity().getHeldItemMainhand(), createPos));
				}
			}
		}
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
	}

	@Override
	public void onMouseEvent(final MouseEvent event, final PlayerOrbis playerOrbis)
	{
		if (event.getButton() == 0 || event.getButton() == 1)
		{
			event.setCanceled(true);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":block_chunk", "inventory"));

		final TileEntityBlockDataContainerRenderer tesr = new TileEntityBlockDataContainerRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlockDataContainerRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityBlockDataContainerRenderer.DummyTile.class);
	}

}
