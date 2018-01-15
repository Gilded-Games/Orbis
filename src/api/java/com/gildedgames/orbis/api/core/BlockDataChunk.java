package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

public class BlockDataChunk implements NBT
{

	private final ChunkPos pos;

	private final BlockDataContainer container;

	public BlockDataChunk(final ChunkPos pos, final BlockDataContainer container)
	{
		this.pos = pos;
		this.container = container;
	}

	public ChunkPos getPos()
	{
		return this.pos;
	}

	public BlockDataContainer getContainer()
	{
		return this.container;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}
}
