package com.gildedgames.orbis.common.capabilities.chunk_renderer;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import java.util.List;

public class ChunkRenderer implements IChunkRendererCapability
{
	private final Region boundingBox;

	private final List<IWorldRenderer> renderers = Lists.newArrayList();

	private boolean hasBeenLoaded;

	public ChunkRenderer()
	{
		this.boundingBox = null;
	}

	public ChunkRenderer(final int chunkX, final int chunkZ)
	{
		this.boundingBox = new Region(new BlockPos(chunkX << 4, 0, chunkZ << 4), new BlockPos((chunkX << 4) + 16, 256, (chunkZ << 4) + 16));
	}

	@Override
	public boolean hasBeenLoaded()
	{
		return this.hasBeenLoaded;
	}

	@Override
	public void load()
	{
		this.hasBeenLoaded = true;
	}

	@Override
	public void render(final World world, final float partialTicks)
	{

	}

	@Override
	public void addRenderer(final IWorldRenderer object)
	{
		this.renderers.add(object);
	}

	@Override
	public boolean removeRenderer(final IWorldRenderer object)
	{
		return this.renderers.remove(object);
	}

	@Override
	public boolean shouldHave(final IWorldRenderer renderer)
	{
		return RegionHelp.intersects2D(renderer.getBoundingBox(), this.boundingBox);
	}

	@Override
	public List<IWorldRenderer> getRenderers()
	{
		return this.renderers;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.boundingBox;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	public static class Storage implements Capability.IStorage<IChunkRendererCapability>
	{
		@Override
		public NBTBase writeNBT(final Capability<IChunkRendererCapability> capability, final IChunkRendererCapability instance, final EnumFacing side)
		{
			final NBTTagCompound out = new NBTTagCompound();
			instance.write(out);

			return out;
		}

		@Override
		public void readNBT(final Capability<IChunkRendererCapability> capability, final IChunkRendererCapability instance, final EnumFacing side,
				final NBTBase nbt)
		{
			final NBTTagCompound input = (NBTTagCompound) nbt;
			instance.read(input);
		}
	}

}
