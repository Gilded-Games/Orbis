package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.lib.data.schedules.IPositionRecord;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

public class StateRecordAccess implements IBlockAccess
{
	protected final World world;

	private IPositionRecord<IBlockState> record;

	private BlockPos min;

	public StateRecordAccess(final World worldIn, IPositionRecord<IBlockState> record, BlockPos min)
	{
		this.world = worldIn;
		this.record = record;
		this.min = min;
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
		int x = pos.getX() - this.min.getX();
		int y = pos.getY() - this.min.getY();
		int z = pos.getZ() - this.min.getZ();

		if (x < this.record.getWidth() && y < this.record.getHeight() && z < this.record.getLength() && x >= 0 && y >= 0 && z >= 0)
		{
			IBlockState state = this.record.get(x, y, z);

			if (state != null)
			{
				return state;
			}
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
