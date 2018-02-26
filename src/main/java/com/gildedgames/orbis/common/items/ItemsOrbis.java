package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber()
public class ItemsOrbis
{
	public static final ItemBlueprint blueprint = new ItemBlueprint();

	public static final ItemBlockDataContainer block_chunk = new ItemBlockDataContainer();

	public static final ItemBlockPalette block_palette = new ItemBlockPalette();

	public static final ItemBlueprintPalette blueprint_palette = new ItemBlueprintPalette();

	public static final ItemEntity entity_item = new ItemEntity();

	public static final CreativeTabs ENTITY_TAB = new CreativeTabs("Entities")
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(Items.DIAMOND_HORSE_ARMOR);
		}
	};

	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event)
	{
		final ItemRegistryHelper items = new ItemRegistryHelper(event.getRegistry());

		items.register("blueprint", blueprint.setCreativeTab(null));
		items.register("block_chunk", block_chunk.setCreativeTab(null));
		items.register("block_palette", block_palette.setCreativeTab(null));
		items.register("blueprint_palette", blueprint_palette.setCreativeTab(null));
		//items.register("entity_item", entity_item.setCreativeTab(ENTITY_TAB));
	}

	private static class ItemRegistryHelper
	{
		private final IForgeRegistry<Item> registry;

		ItemRegistryHelper(final IForgeRegistry<Item> registry)
		{
			this.registry = registry;
		}

		private void register(final String registryName, final Item item)
		{
			item.setUnlocalizedName(OrbisCore.MOD_ID + "." + registryName);

			item.setRegistryName(OrbisCore.MOD_ID, registryName);
			this.registry.register(item);
		}

		private void registerBlock(final Block block)
		{
			final ItemBlock metaItemBlock = new ItemBlock(block);
			this.register(metaItemBlock);
		}

		private void register(final ItemBlock item)
		{
			item.setRegistryName(item.getBlock().getRegistryName());
			item.setUnlocalizedName(item.getBlock().getUnlocalizedName());
			this.registry.register(item);
		}
	}

}
