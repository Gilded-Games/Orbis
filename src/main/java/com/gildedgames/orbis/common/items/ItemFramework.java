package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis_api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis_api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis_api.data.management.IDataIdentifier;
import com.gildedgames.orbis_api.data.management.IDataMetadata;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityFrameworkRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import com.gildedgames.orbis.common.util.RaytraceHelp;
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
public class ItemFramework extends Item implements ModelRegisterCallback, ItemStackInput
{
	@SideOnly(Side.CLIENT)
	private static TileEntityFrameworkRenderer.BakedModel dummyModel;

	public ItemFramework()
	{
		super();

		this.setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":framework", "inventory"), dummyModel);
	}

	public static void setDataId(final ItemStack stack, final IDataIdentifier id)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		funnel.set("data_id", id);
	}

	public static IDataIdentifier getDataId(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("data_id"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		final IDataIdentifier id = funnel.get("data_id");

		return id;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":framework", "inventory"));

		final TileEntityFrameworkRenderer tesr = new TileEntityFrameworkRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFrameworkRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityFrameworkRenderer.DummyTile.class);
	}

	@Override
	public String getItemStackDisplayName(final ItemStack stack)
	{
		final IDataIdentifier id = ItemFramework.getDataId(stack);

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
			final BlockPos pos = RaytraceHelp.doOrbisRaytrace(playerOrbis, playerOrbis.raytraceWithRegionSnapping());

			if (!pos.equals(playerOrbis.powers().getBlueprintPower().getPrevPlacingPos()))
			{
				playerOrbis.powers().getBlueprintPower().setPrevPlacingPos(pos);
				final BlockPos createPos = playerOrbis.raytraceNoSnapping();

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
