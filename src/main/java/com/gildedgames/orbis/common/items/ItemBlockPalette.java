package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlockPaletteRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.block.BlockFilterLayer;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBlockPalette extends Item implements ModelRegisterCallback
{

	@SideOnly(Side.CLIENT)
	private static TileEntityBlockPaletteRenderer.BakedModel dummyModel;

	public ItemBlockPalette()
	{
		super();

		this.setHasSubtypes(true);
	}

	public static void setFilterLayer(final ItemStack stack, final BlockFilterLayer layer)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		funnel.set("layer", layer);
	}

	public static BlockFilterLayer getFilterLayer(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("layer"))
		{
			return null;
		}

		final NBTFunnel funnel = new NBTFunnel(stack.getTagCompound());

		return funnel.get("layer");
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":block_palette", "inventory"), dummyModel);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":block_palette", "inventory"));

		final TileEntityBlockPaletteRenderer tesr = new TileEntityBlockPaletteRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlockPaletteRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		//TODO: Replace with TileEntityItemStackRenderer, instead of a tile entity hack
		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityBlockPaletteRenderer.DummyTile.class);
	}
}
