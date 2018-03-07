package com.gildedgames.orbis.api.data.schedules;

import net.minecraft.nbt.NBTTagCompound;

public class LayerOptions implements ILayerOptions
{

	private boolean choosesPerBlock;

	private float edgeNoise;

	public LayerOptions()
	{

	}

	@Override
	public ILayerOptions setChoosesPerBlock(boolean choosesPerBlock)
	{
		this.choosesPerBlock = choosesPerBlock;

		return this;
	}

	@Override
	public boolean choosesPerBlock()
	{
		return this.choosesPerBlock;
	}

	@Override
	public float getEdgeNoise()
	{
		return this.edgeNoise;
	}

	@Override
	public ILayerOptions setEdgeNoise(float edgeNoise)
	{
		this.edgeNoise = edgeNoise;

		return this;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		tag.setBoolean("choosesPerBlock", this.choosesPerBlock);
		tag.setFloat("edgeNoise", this.edgeNoise);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		this.choosesPerBlock = tag.getBoolean("choosesPerBlock");
		this.edgeNoise = tag.getFloat("edgeNoise");
	}
}
