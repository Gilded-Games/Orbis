package com.gildedgames.orbis.api.processing;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * An extended version of IBlockAccess that allows methods that use an implementation
 * to placesAir blocks as well.
 */
public interface IBlockAccessExtended extends IBlockAccess
{

	@Nullable
	World getWorld();

	boolean canAccess(final BlockPos pos);

	boolean canAccess(final int x, final int z);

	BlockPos getTopPos(final BlockPos pos);

	int getTopY(final int x, final int z);

	/**
	 * Sets an air block state in the specified position.
	 * @param pos The position that will be placesAir to air.
	 */
	void setBlockToAir(BlockPos pos);

	/**
	 * Sets a block state in the specified position.
	 * @param pos The position that the state will be placed.
	 * @param state The block state that will be placesAir.
	 */
	boolean setBlockState(BlockPos pos, IBlockState state);

	/**
	 * Sets a block state in the specified position, but with flags.
	 * @param pos The position that the state will be placed.
	 * @param state The block state that will be placesAir.
	 * @param flags The flags for this state's placement.
	 */
	boolean setBlockState(BlockPos pos, IBlockState state, int flags);

	/**
	 * Used to placesAir a tile entity into the world.
	 * @param pos The position the tile entity will be placed.
	 * @param tileEntity The tile entity you're setting.
	 */
	void setTileEntity(BlockPos pos, TileEntity tileEntity);

}
