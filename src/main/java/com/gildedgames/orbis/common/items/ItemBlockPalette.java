package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.api.block.BlockDataWithConditions;
import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.block.BlockFilterLayer;
import com.gildedgames.orbis.api.block.BlockFilterType;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityBlockPaletteRenderer;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.util.CreationDataOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemBlockPalette extends Item implements ModelRegisterCallback, IShapeSelector
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

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":block_palette", "inventory"));

		final TileEntityBlockPaletteRenderer tesr = new TileEntityBlockPaletteRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlockPaletteRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityBlockPaletteRenderer.DummyTile.class);
	}

	@Override
	public boolean isSelectorActive(final PlayerOrbis playerOrbis, final World world)
	{
		return true;
	}

	@Override
	public boolean canSelectShape(final PlayerOrbis playerOrbis, final IShape shape, final World world)
	{
		final WorldObjectManager manager = WorldObjectManager.get(world);
		final IWorldObjectGroup group = manager.getGroup(0);

		return group.getIntersectingShapes(Blueprint.class, shape).size() == 1 || !playerOrbis.powers().isScheduling();
	}

	@Override
	public void onSelect(final PlayerOrbis playerOrbis, final IShape selectedShape, final World world, BlockPos start, BlockPos end)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		final CreationDataOrbis creationData = new CreationDataOrbis(world, playerOrbis.getEntity());

		creationData.schedules(playerOrbis.powers().isScheduling());

		final BlockFilterLayer layer = ItemBlockPalette.getFilterLayer(held);

		if (playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getReplacePower())
		{
			layer.setFilterType(BlockFilterType.ALL_EXCEPT);

			layer.setRequiredBlocks(new BlockDataWithConditions(Blocks.AIR.getDefaultState(), 1.0F));
		}
		else if (playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getDeletePower())
		{
			layer.setFilterType(BlockFilterType.ONLY);

			layer.setRequiredBlocks(layer.getReplacementBlocks());
			layer.setReplacementBlocks(new BlockDataWithConditions(Blocks.AIR.getDefaultState(), 1.0F));
		}

		final BlockFilter filter = new BlockFilter(layer);

		filter.apply(selectedShape, creationData, true);
	}
}
