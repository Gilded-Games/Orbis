package com.gildedgames.orbis.client.renderers.blueprint;

import com.gildedgames.orbis_api.block.BlockDataContainer;
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

import java.util.Arrays;

public class BlockDataContainerAccess implements IBlockAccess
{
	protected final BlockDataContainer container;

	protected final IBlockAccess world;

	private final int[] lightvalues;

	private final int volume;

	public BlockDataContainerAccess(final BlockDataContainer container, final World worldIn)
	{
		this.container = container;
		this.world = worldIn;

		this.volume = container.getVolume();
		this.lightvalues = new int[this.volume];
		Arrays.fill(this.lightvalues, -1);
	}

	public IBlockAccess getWorld()
	{
		return this.world;
	}

	@Override
	public TileEntity getTileEntity(final BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(final BlockPos pos, final int p_175626_2_)
	{
		final int index = this.getIndex(pos);

		if (index < 0 || index >= this.volume)
		{
			return 0;
		}

		int value = this.lightvalues[index];

		if (value == -1)
		{
			value = 0;
			this.lightvalues[index] = value;
		}

		return value;
	}

	private int getIndex(final BlockPos pos)
	{
		return pos.getZ() + this.container.getLength() * pos.getY() + this.container.getLength() * this.container.getHeight() * pos.getX();
	}

	@Override
	public IBlockState getBlockState(final BlockPos pos)
	{
		if (!RegionHelp.contains(this.container, pos))
		{
			return Blocks.AIR.getDefaultState();
		}

		final IBlockState data = this.container.getBlockState(pos);

		if (data == null)
		{
			return Blocks.AIR.getDefaultState();
		}

		return data;
	}

	@Override
	public boolean isAirBlock(final BlockPos pos)
	{
		final IBlockState data = this.container.getBlockState(pos);

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
