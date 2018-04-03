package com.gildedgames.orbis.api.data.shapes;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.LineHelp;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LineShape implements IShape
{
	private World world;

	private BlockPos start;

	private BlockPos end;

	private int lineRadius;

	private Iterable<BlockPos.MutableBlockPos> data;

	private LineShape(final World world)
	{
		this.world = world;
	}

	public LineShape(final BlockPos start, final BlockPos end)
	{
		this(start, end, 2);
	}

	public LineShape(final BlockPos start, final BlockPos end, final int lineRadius)
	{
		this.start = start;
		this.end = end;

		this.lineRadius = lineRadius;
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> createShapeData()
	{
		return LineHelp.createLinePositions(this.lineRadius, this.start, this.end);
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("start", this.start);
		funnel.setPos("end", this.end);

		tag.setInteger("lineRadius", this.lineRadius);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.start = funnel.getPos("start");
		this.end = funnel.getPos("end");

		this.lineRadius = tag.getInteger("lineRadius");
	}

	@Override
	public IShape rotate(final Rotation rotation, final IRegion regionIn)
	{
		final BlockPos newStart = RotationHelp.rotate(this.start, regionIn, rotation);
		final BlockPos newEnd = RotationHelp.rotate(this.end, regionIn, rotation);

		return new LineShape(newStart, newEnd, this.lineRadius);
	}

	@Override
	public IShape translate(final int x, final int y, final int z)
	{
		return new LineShape(this.start.add(x, y, z), this.end.add(x, y, z), this.lineRadius);
	}

	@Override
	public IShape translate(final BlockPos pos)
	{
		return this.translate(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public IRegion getBoundingBox()
	{
		return new Region(this.start, this.end);
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
	public boolean contains(final BlockPos pos)
	{
		return this.contains(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> getShapeData()
	{
		if (this.data == null)
		{
			this.data = this.createShapeData();
		}

		return this.data;
	}

}
