package com.gildedgames.orbis.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class FakeOrbisFloorWorld implements IBlockAccess
{
    private IBlockState state;

    public FakeOrbisFloorWorld(IBlockState state)
    {
        this.state = state;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos)
    {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue)
    {
        return 0;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos)
    {
        return state;
    }

    @Override
    public boolean isAirBlock(BlockPos pos)
    {
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public Biome getBiome(BlockPos pos)
    {
        return Biomes.PLAINS;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return 0;
    }

    @Override
    public WorldType getWorldType()
    {
        return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return state.isSideSolid(this, pos, side);
    }
}