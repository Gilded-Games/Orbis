package com.gildedgames.orbis.client.renderers.blueprint;

import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis_api.block.BlockData;
import com.gildedgames.orbis_api.util.RegionHelp;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class BlueprintRegionAccess implements IBlockAccess
{
	protected final Blueprint blueprint;

	protected final IBlockAccess world;

	public BlueprintRegionAccess(final Blueprint blueprint, final World worldIn)
	{
		this.blueprint = blueprint;
		this.world = worldIn;
	}

	@Override
	public TileEntity getTileEntity(final BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(final BlockPos p_175626_1_, final int p_175626_2_)
	{
		return 0;
	}

	@Override
	public IBlockState getBlockState(final BlockPos pos)
	{
		if (!RegionHelp.contains(this.blueprint, pos.add(this.blueprint.getMin())))
		{
			return Blocks.AIR.getDefaultState();
		}

		final IBlockState data = this.blueprint.getBlockDataContainer().getBlockState(pos);

		if (data == null)
		{
			return Blocks.AIR.getDefaultState();
		}

		return data.withRotation(this.blueprint.getRotation());
	}

	@Override
	public boolean isAirBlock(final BlockPos pos)
	{
		final IBlockState data = this.blueprint.getBlockDataContainer().getBlockState(pos);

		return data.getMaterial() == Material.AIR;
	}

	@Override
	public Biome getBiome(final BlockPos pos)
	{
		return this.world.getBiome(pos);
	}

	@Override
	public int getStrongPower(final BlockPos pos, final EnumFacing direction)
	{
		final IBlockState iblockstate = this.getBlockState(pos);
		return iblockstate.getBlock().getStrongPower(iblockstate, this, pos, direction);
	}

	@Override
	public WorldType getWorldType()
	{
		return this.world.getWorldType();
	}

	@Override
	public boolean isSideSolid(final BlockPos pos, final EnumFacing side, final boolean _default)
	{
		final IBlockState iblockstate = this.getBlockState(pos);
		return iblockstate.getBlock().isSideSolid(iblockstate, this, pos, side);
	}
}
