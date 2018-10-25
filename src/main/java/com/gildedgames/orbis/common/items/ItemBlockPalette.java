package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlockPaletteRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.block.BlockDataWithConditions;
import com.gildedgames.orbis_api.block.BlockFilterLayer;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);

		BlockFilterLayer layer = ItemBlockPalette.getFilterLayer(stack);

		if (layer != null)
		{
			for (BlockDataWithConditions block : layer.getReplacementBlocks())
			{
				ItemStack blockStack = new ItemStack(block.getBlockState().getBlock(), 1,
						block.getBlockState().getBlock().getMetaFromState(block.getBlockState()));

				String blockName = I18n.format(Item.getItemFromBlock(block.getBlockState().getBlock()).getUnlocalizedName(blockStack) + ".name");

				tooltip.add(
						MathHelper.floor(block.getReplaceCondition().getWeight()) + " " + blockName);
			}
		}
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
