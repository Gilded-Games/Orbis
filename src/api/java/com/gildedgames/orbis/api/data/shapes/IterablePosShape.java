package com.gildedgames.orbis.api.data.shapes;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class IterablePosShape implements IShape
{

	private Iterable<BlockPos.MutableBlockPos> positions;

	private IRegion bb;

	public IterablePosShape(Iterable<BlockPos.MutableBlockPos> positions, BlockPos pos, int width, int height, int length)
	{
		this.positions = positions;

		this.bb = new Region(pos, pos.add(width, height, length));
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> createShapeData()
	{
		return this.positions;
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> getShapeData()
	{
		return this.positions;
	}

	@Override
	public IShape rotate(Rotation rotation, IRegion in)
	{
		return this;
	}

	@Override
	public IShape translate(int x, int y, int z)
	{
		return this;
	}

	@Override
	public IShape translate(BlockPos pos)
	{
		return this;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.bb;
	}

	@Override
	public boolean contains(final int x, final int y, final int z)
	{
		boolean flag = false;

		for (final BlockPos.MutableBlockPos pos : this.getShapeData())
		{
			if (pos.getX() == x && pos.getY() == y && pos.getZ() == z)
			{
				flag = true;
				break;
			}
		}

		return flag;
	}

	@Override
	public boolean contains(BlockPos pos)
	{
		return this.contains(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void write(NBTTagCompound tag)
	{

	}

	@Override
	public void read(NBTTagCompound tag)
	{

	}
}
