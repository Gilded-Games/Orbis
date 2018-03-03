package com.gildedgames.orbis.common.world.orbis_instance;

import com.gildedgames.orbis.common.OrbisCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderOrbis extends WorldProviderSurface
{

	public static DimensionType ORBIS;

	public WorldProviderOrbis()
	{

	}

	public static void preInit()
	{
		WorldProviderOrbis.ORBIS = DimensionType.register("Orbis", "_orbis",
				OrbisCore.CONFIG.getOrbisDimId(), WorldProviderOrbis.class, false);
	}

	@Override
	protected void init()
	{
		this.hasSkyLight = true;
		this.biomeProvider = new BiomeProviderSingle(BiomesOrbis.INSTANCED_ZONE);
	}

	@Override
	public Biome getBiomeForCoords(final BlockPos pos)
	{
		return this.biomeProvider.getBiome(pos);
	}

	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new ChunkProviderOrbis(this.world, this.world.getSeed());
	}

	@Override
	public boolean canCoordinateBeSpawn(final int x, final int z)
	{
		return true;
	}

	@Override
	public boolean canRespawnHere()
	{
		return true;
	}

	@Override
	public boolean isSurfaceWorld()
	{
		return false;
	}

	@Override
	public int getRespawnDimension(final EntityPlayerMP player)
	{
		return 0;
	}

	@Override
	public String getSaveFolder()
	{
		return super.getSaveFolder();
	}

	@Override
	public double getHorizon()
	{
		return 0.0;
	}

	@Override
	public DimensionType getDimensionType()
	{
		return WorldProviderOrbis.ORBIS;
	}

	@Override
	public double getVoidFogYFactor()
	{
		return 100;
	}

	@Override
	public boolean isSkyColored()
	{
		return false;
	}

	@Override
	public Vec3d getSkyColor(final Entity par1Entity, final float par2)
	{
		return new Vec3d(255D, 255D, 255D);
	}

	@Override
	public float[] calcSunriseSunsetColors(final float f, final float f1)
	{
		return null;
	}

	@Override
	public boolean canDropChunk(final int x, final int z)
	{
		return true;
	}

	@Override
	public boolean canMineBlock(final EntityPlayer player, final BlockPos pos)
	{
		// Doing this will prevent buckets from being used when not GM1, but will not affect block
		// placement/destruction (only called in World#isBlockModifiable(EntityPlayer, Blockpos) )
		return (player.capabilities.isCreativeMode);
	}

	@Override
	public float calculateCelestialAngle(final long worldTime, final float partialTicks)
	{
		return 255F;
	}

	@Override
	public float getSunBrightnessFactor(float par1)
	{
		return 1.0F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(float p_76562_1_, float p_76562_2_)
	{
		return new Vec3d(255, 255, 255);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight()
	{
		return 0.0F;
	}

	@Override
	public BlockPos getSpawnCoordinate()
	{
		return new BlockPos(0, 2, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesXZShowFog(int x, int z)
	{
		return false;
	}

}
