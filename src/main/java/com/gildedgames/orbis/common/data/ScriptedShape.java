package com.gildedgames.orbis.common.data;

import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.data.shapes.AbstractShape;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import net.benmann.evald.Evald;
import net.benmann.evald.Library;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScriptedShape extends AbstractShape
{

	private BlockPos start, min;

	private BlockPos end, max;

	private boolean centered;

	private Iterable<BlockPos.MutableBlockPos> data;

	private String inequalityMathExpression;

	private Evald evald;

	private int aX, aY, aZ;

	private ScriptedShape(final World world)
	{
		super(world);
	}

	public ScriptedShape(final BlockPos start, final BlockPos end, final boolean centered, String inequalityMathExpression, int aX, int aY, int aZ, Evald evald)
	{
		this.start = start;
		this.end = end;

		final int radius = (int) Math.sqrt(this.start.distanceSq(this.end));

		this.setBoundingBox(centered ?
				new Region(new BlockPos(-radius, -radius, -radius).add(this.start), new BlockPos(radius, radius, radius).add(this.start)) :
				new Region(start, end));

		this.centered = centered;
		this.inequalityMathExpression = inequalityMathExpression;

		this.min = RegionHelp.getMin(this.getBoundingBox().getMin(), this.getBoundingBox().getMax());
		this.max = RegionHelp.getMax(this.getBoundingBox().getMin(), this.getBoundingBox().getMax());

		this.evald = evald;

		this.aX = aX;
		this.aY = aY;
		this.aZ = aZ;
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

		tag.setString("exp", this.inequalityMathExpression);
	}

	@Override
	public void readShape(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.start = funnel.getPos("start");
		this.end = funnel.getPos("end");

		this.centered = tag.getBoolean("centered");

		this.inequalityMathExpression = tag.getString("exp");

		this.min = RegionHelp.getMin(this.getBoundingBox().getMin(), this.getBoundingBox().getMax());
		this.max = RegionHelp.getMax(this.getBoundingBox().getMin(), this.getBoundingBox().getMax());

		this.evald = new Evald();

		this.evald.addLibrary(Library.ALL);

		this.aX = this.evald.addVariable("x");
		this.aY = this.evald.addVariable("y");
		this.aZ = this.evald.addVariable("z");

		this.evald.parse(this.inequalityMathExpression);
	}

	@Override
	public IShape rotate(final Rotation rotation, final IRegion in)
	{
		return this;
	}

	@Override
	public IShape translate(final int x, final int y, final int z)
	{
		return new ScriptedShape(this.start.add(x, y, z), this.end.add(x, y, z), this.centered, this.inequalityMathExpression, this.aX, this.aY, this.aZ,
				this.evald);
	}

	@Override
	public IShape translate(final BlockPos pos)
	{
		return this.translate(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean contains(final int x, final int y, final int z)
	{
		double xDif = x - this.min.getX();
		double yDif = y - this.min.getY();
		double zDif = z - this.min.getZ();

		double width = Math.max(1, this.max.getX() - this.min.getX());
		double height = Math.max(1, this.max.getY() - this.min.getY());
		double length = Math.max(1, this.max.getZ() - this.min.getZ());

		this.evald.setVariable(this.aX, ((xDif / width) * 2.0D) - 1.0D);
		this.evald.setVariable(this.aY, ((yDif / height) * 2.0D) - 1.0D);
		this.evald.setVariable(this.aZ, ((zDif / length) * 2.0D) - 1.0D);

		return this.evald.evaluate() <= 0;
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
