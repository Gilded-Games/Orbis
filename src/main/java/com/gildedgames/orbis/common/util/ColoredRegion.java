package com.gildedgames.orbis.common.util;

import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.common.world_objects.IColored;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class ColoredRegion extends Region implements IColored
{
	private int color;

	private ColoredRegion()
	{

	}

	public ColoredRegion(final IRegion region)
	{
		super(region);
	}

	public ColoredRegion(final BlockPos pos)
	{
		super(pos);
	}

	public ColoredRegion(final BlockPos corner1, final BlockPos corner2)
	{
		super(corner1, corner2);
	}

	public ColoredRegion(final IDimensions dimensions)
	{
		super(dimensions);
	}

	@Override
	public int getColor()
	{
		return this.color;
	}

	public ColoredRegion setColor(int color)
	{
		this.color = color;

		return this;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		super.write(tag);

		tag.setInteger("color", this.color);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		super.read(tag);

		this.color = tag.getInteger("color");
	}
}
