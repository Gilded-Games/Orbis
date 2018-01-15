package com.gildedgames.orbis.common.capabilities.chunk_renderer;

public interface IChunkRendererProvider
{
	IChunkRendererCapability get(final int chunkX, final int chunkZ);
}
