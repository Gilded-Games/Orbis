package com.gildedgames.orbis.api.data.shapes;

import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CuboidOutskirtShape extends AbstractShape
{

	private BlockPos start;

	private BlockPos end;

	private boolean centered;

	private Iterable<BlockPos.MutableBlockPos> data;

	private BlockPos renderMin, renderMax;

	private CuboidOutskirtShape(final World world)
	{
		super(world);
	}

	public CuboidOutskirtShape(final BlockPos start, final BlockPos end, final boolean centered)
	{
		this.start = start;
		this.end = end;

		final int radius = (int) Math.sqrt(this.start.distanceSq(this.end));

		this.setBoundingBox(centered ?
				new Region(new BlockPos(-radius, -radius, -radius).add(this.start), new BlockPos(radius, radius, radius).add(this.start)) :
				new Region(start, end));

		this.centered = centered;

		this.renderMin = this.getBoundingBox().getMin();
		this.renderMax = this.getBoundingBox().getMax();
	}

	@Override
	public BlockPos getRenderBoxMin()
	{
		return this.getBoundingBox().getMin();
	}

	@Override
	public BlockPos getRenderBoxMax()
	{
		return this.getBoundingBox().getMax();
	}

	@Override
	public void writeShape(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("start", this.start);
		funnel.setPos("end", this.end);

		tag.setBoolean("centered", this.centered);
	}

	@Override
	public void readShape(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.start = funnel.getPos("start");
		this.end = funnel.getPos("end");

		this.centered = tag.getBoolean("centered");
	}

	@Override
	public IShape rotate(final Rotation rotation, final IRegion in)
	{
		return this;
	}

	@Override
	public IShape translate(final int x, final int y, final int z)
	{
		return new CuboidOutskirtShape(this.start.add(x, y, z), this.end.add(x, y, z), this.centered);
	}

	@Override
	public IShape translate(final BlockPos pos)
	{
		return this.translate(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean contains(final int x, final int y, final int z)
	{
		if (this.createFromCenter() || !this.isUniform())
		{
			boolean north = x == this.getBoundingBox().getMin().getX();
			boolean south = x == this.getBoundingBox().getMax().getX();
			boolean east = z == this.getBoundingBox().getMax().getZ();
			boolean west = z == this.getBoundingBox().getMin().getZ();
			boolean up = y == this.getBoundingBox().getMax().getY();
			boolean down = y == this.getBoundingBox().getMin().getY();

			return RegionHelp.contains(this.getBoundingBox(), x, y, z) && (north || south || east || west || up || down);
		}
		else
		{
			final int minSize = Math.min(this.getBoundingBox().getWidth(), this.getBoundingBox().getLength());

			final int xDif = Math.abs(x - this.start.getX());
			final int zDif = Math.abs(z - this.start.getZ());

			return RegionHelp.contains(this.getBoundingBox(), x, y, z) && xDif < minSize && zDif < minSize && xDif + 1 >= minSize && zDif + 1 >= minSize;
		}
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
