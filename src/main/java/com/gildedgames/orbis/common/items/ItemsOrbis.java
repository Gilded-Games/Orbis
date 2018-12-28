package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Objects;

@Mod.EventBusSubscriber()
public class ItemsOrbis
{
	public static final ItemBlueprint blueprint = new ItemBlueprint();

	public static final ItemBlockDataContainer block_chunk = new ItemBlockDataContainer();

	public static final ItemBlockPalette block_palette = new ItemBlockPalette();

	public static final ItemBlueprintPalette blueprint_palette = new ItemBlueprintPalette();

	public static final ItemFramework framework = new ItemFramework();

	public static final ItemBlueprintStacker blueprint_stacker = new ItemBlueprintStacker();

	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event)
	{
		final ItemRegistryHelper items = new ItemRegistryHelper(event.getRegistry());

		items.register("blueprint", blueprint);
		items.register("block_chunk", block_chunk);
		items.register("block_palette", block_palette);
		items.register("blueprint_palette", blueprint_palette);
		items.register("framework", framework);
		items.register("blueprint_stacker", blueprint_stacker);
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
			item.setTranslationKey(OrbisCore.MOD_ID + "." + registryName);

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
			item.setRegistryName(Objects.requireNonNull(item.getBlock().getRegistryName()));
			item.setTranslationKey(item.getBlock().getTranslationKey());
			this.registry.register(item);
		}
	}

}
