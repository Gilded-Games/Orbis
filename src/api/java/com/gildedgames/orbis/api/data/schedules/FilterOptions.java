package com.gildedgames.orbis.api.data.schedules;

import net.minecraft.nbt.NBTTagCompound;

public class FilterOptions implements IFilterOptions
{

	public static final IFilterOptions CHOOSES_PER_BLOCK = new FilterOptions().setChoosesPerBlock(true);

	private boolean choosesPerBlock;

	private float edgeNoise;

	public FilterOptions()
	{

	}

	@Override
	public IFilterOptions setChoosesPerBlock(boolean choosesPerBlock)
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
	public IFilterOptions setEdgeNoise(float edgeNoise)
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
