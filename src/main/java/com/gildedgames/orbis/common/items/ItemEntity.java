package com.gildedgames.orbis.common.items;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.renderers.tiles.TileEntityEntityItemRenderer;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.items.util.ItemStackInput;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ItemEntity extends Item implements ModelRegisterCallback, ItemStackInput
{
	@SideOnly(Side.CLIENT)
	private static TileEntityEntityItemRenderer.BakedModel dummyModel;

	public ItemEntity()
	{
		super();

		this.setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onModelBake(final ModelBakeEvent event)
	{
		//event.getModelRegistry().putObject(new ModelResourceLocation(OrbisCore.MOD_ID + ":entity", "inventory"), dummyModel);
	}

	public static void setEntity(final ItemStack stack, ResourceLocation registryName)
	{
		if (stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		stack.getTagCompound().setString("registryName", registryName.toString());
	}

	public static ResourceLocation getEntityRegistryName(final ItemStack stack)
	{
		if (stack.getTagCompound() == null || !stack.getTagCompound().hasKey("registryName"))
		{
			return null;
		}

		ResourceLocation registryName = new ResourceLocation(stack.getTagCompound().getString("registryName"));

		return registryName;
	}

	public static EntityEntry getEntityEntry(final ItemStack stack)
	{
		ResourceLocation registryName = getEntityRegistryName(stack);

		if (registryName == null)
		{
			return null;
		}

		return ForgeRegistries.ENTITIES.getValue(registryName);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
	{
		if (this.isInCreativeTab(tab))
		{
			for (Map.Entry<ResourceLocation, EntityEntry> e : ForgeRegistries.ENTITIES.getEntries())
			{
				ResourceLocation registryName = e.getKey();
				EntityEntry entry = e.getValue();

				if (entry.getEgg() != null)
				{
					ItemStack stack = new ItemStack(this);
					setEntity(stack, registryName);

					items.add(stack);
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		/*ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(OrbisCore.MOD_ID + ":entity", "inventory"));

		final TileEntityEntityItemRenderer tesr = new TileEntityEntityItemRenderer();

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEntityItemRenderer.DummyTile.class, tesr);
		dummyModel = tesr.baked;

		ForgeHooksClient.registerTESRItemStack(this, 0, TileEntityEntityItemRenderer.DummyTile.class);*/
	}

	@Override
	public String getItemStackDisplayName(final ItemStack stack)
	{
		EntityEntry entry = getEntityEntry(stack);

		if (entry != null)
		{
			return entry.getName();
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

		if (Minecraft.getMinecraft().currentScreen != null)
		{
			return;
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
