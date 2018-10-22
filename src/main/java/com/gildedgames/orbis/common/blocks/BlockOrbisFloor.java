package com.gildedgames.orbis.common.blocks;

import com.gildedgames.orbis.client.ModelRegisterCallback;
import com.gildedgames.orbis.client.model.ModelOrbisFloor;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.OrbisAPI;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(Side.CLIENT)
public class BlockOrbisFloor extends Block implements ModelRegisterCallback
{
	public BlockOrbisFloor()
	{
		super(Material.BARRIER, MapColor.SNOW);

		this.setHardness(-1F);
		this.setBlockUnbreakable();
		this.setResistance(6000001.0F);
	}

	@Override
	@Nonnull
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity)
	{
		if (OrbisAPI.isClient() && ModelOrbisFloor.currentMimicBlock != null)
		{
			ModelOrbisFloor.currentMimicBlock.getBlock().getSoundType(ModelOrbisFloor.currentMimicBlock, world, pos, entity);
		}
		return super.getSoundType(state, world, pos, entity);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity)
	{
		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		world.setBlockToAir(pos);
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
	{
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		return false;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return ItemStack.EMPTY;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel()
	{
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(new ResourceLocation(OrbisCore.MOD_ID, "orbis_floor"), "inventory"));
	}
}