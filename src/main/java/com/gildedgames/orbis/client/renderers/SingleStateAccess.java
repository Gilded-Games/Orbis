package com.gildedgames.orbis.client.renderers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class SingleStateAccess implements IBlockAccess
{
	protected final IBlockAccess world;

	protected IBlockState state;

	private BlockPos pos;

	public SingleStateAccess(final IBlockState state, final World worldIn, final BlockPos pos)
	{
		this.state = state;
		this.world = worldIn;
		this.pos = pos;
	}

	public void setState(final IBlockState state)
	{
		this.state = state;
	}

	public void setPos(final BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public TileEntity getTileEntity(final BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(final BlockPos p_175626_1_, final int p_175626_2_)
	{
		return -1;
	}

	@Override
	public IBlockState getBlockState(final BlockPos pos)
	{
		if (this.pos.equals(pos))
		{
			return this.state;
		}

		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isAirBlock(final BlockPos pos)
	{
		final IBlockState state = this.getBlockState(pos);

		return state == Blocks.AIR.getDefaultState();
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
