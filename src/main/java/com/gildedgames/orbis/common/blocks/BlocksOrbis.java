package com.gildedgames.orbis.common.blocks;

import com.gildedgames.orbis.common.OrbisCore;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Set;

@Mod.EventBusSubscriber()
public class BlocksOrbis
{
	public static final Set<ItemBlock> ITEM_BLOCKS = Sets.newLinkedHashSet();

	public static final Block orbis_floor = new BlockOrbisFloor();

	@SubscribeEvent
	public static void onRegisterBlocks(final RegistryEvent.Register<Block> event)
	{
		final BlockRegistryHelper r = new BlockRegistryHelper(event.getRegistry());

		r.register("orbis_floor", orbis_floor);
	}

	@SubscribeEvent
	public static void onRegisterItems(final RegistryEvent.Register<Item> event)
	{
		for (final ItemBlock itemBlock : ITEM_BLOCKS)
		{
			event.getRegistry().register(itemBlock);
		}
	}

	private static class BlockRegistryHelper
	{
		private final IForgeRegistry<Block> registry;

		BlockRegistryHelper(final IForgeRegistry<Block> registry)
		{
			this.registry = registry;
		}

		private void register(final String registryName, final Block block)
		{
			block.setUnlocalizedName(OrbisCore.MOD_ID + "." + registryName);

			block.setRegistryName(OrbisCore.MOD_ID, registryName);
			this.registry.register(block);

			final ItemBlock item;

			item = new ItemBlock(block);

			item.setRegistryName(OrbisCore.MOD_ID, registryName).setUnlocalizedName(OrbisCore.MOD_ID + "." + registryName);

			ITEM_BLOCKS.add(item);
		}
	}
}
