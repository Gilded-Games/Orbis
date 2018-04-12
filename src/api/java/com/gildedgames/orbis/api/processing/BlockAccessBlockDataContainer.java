package com.gildedgames.orbis.api.processing;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class BlockAccessBlockDataContainer implements IBlockAccessExtended
{
	private final World world;

	private final BlockDataContainer container;

	public BlockAccessBlockDataContainer(final World world, BlockDataContainer container)
	{
		this.world = world;
		this.container = container;
	}

	@Nullable
	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public boolean canAccess(final BlockPos pos)
	{
		return pos.getX() >= 0 && pos.getX() < this.container.getWidth() && pos.getY() >= 0 && pos.getY() < this.container.getHeight() && pos.getZ() >= 0
				&& pos.getZ() < this.container.getLength();
	}

	@Override
	public boolean canAccess(final int x, final int z)
	{
		return this.canAccess(new BlockPos(x, 0, z));
	}

	@Override
	public boolean canAccess(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
	{
		return minX >= 0 && maxX < this.container.getWidth() && minY >= 0 && maxY < this.container.getHeight() && minZ >= 0
				&& maxZ < this.container.getLength();
	}

	@Override
	public BlockPos getTopPos(final BlockPos pos)
	{
		//TODO: use block data container
		return this.world.getTopSolidOrLiquidBlock(pos);
	}

	@Override
	public int getTopY(final int x, final int z)
	{
		//TODO: use block data container
		return this.world.getHeight(x, z);
	}

	@Override
	public void setBlockToAir(final BlockPos pos)
	{
		this.container.set(new BlockData(Blocks.AIR.getDefaultState()), pos);
	}

	@Override
	public boolean setBlockState(final BlockPos pos, final IBlockState state)
	{
		this.container.set(new BlockData(state), pos);

		return true;
	}

	@Override
	public boolean setBlockState(final BlockPos pos, final IBlockState state, final int flags)
	{
		this.container.set(new BlockData(state), pos);

		return true;
	}

	@Override
	public void setTileEntity(final BlockPos pos, final TileEntity tileEntity)
	{

	}

	@Override
	public void spawnEntity(Entity entity)
	{

	}

	@Override
	public Biome getServerBiome(BlockPos pos)
	{
		return null;
	}

	@Nullable
	@Override
	public TileEntity getTileEntity(final BlockPos pos)
	{
		return null;
	}

	@Override
	public int getCombinedLight(final BlockPos pos, final int lightValue)
	{
		return 0;
	}

	@Override
	public IBlockState getBlockState(final BlockPos pos)
	{
		return this.container.get(pos).getBlockState();
	}

	@Override
	public boolean isAirBlock(final BlockPos pos)
	{
		return this.getBlockState(pos) == Blocks.AIR.getDefaultState();
	}

	@Override
	public Biome getBiome(final BlockPos pos)
	{
		return this.world.getBiome(pos);
	}

	@Override
	public int getStrongPower(final BlockPos pos, final EnumFacing direction)
	{
		return 0;
	}

	@Override
	public WorldType getWorldType()
	{
		return this.world.getWorldType();
	}

	@Override
	public boolean isSideSolid(final BlockPos pos, final EnumFacing side, final boolean _default)
	{
		return this.getBlockState(pos).isSideSolid(this, pos, side);
	}
}
