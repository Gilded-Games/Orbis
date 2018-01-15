package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FilterRecord implements IPositionRecord<BlockFilter>
{
	private final List<IPositionRecordListener> listeners = Lists.newArrayList();

	private BlockFilter[] filterInstances;

	private int[] markedPositions;

	private int width, height, length, volume;

	private IRegion boundingBox;

	private Iterable<BlockPos.MutableBlockPos> data;

	private FilterRecord()
	{

	}

	public FilterRecord(final int width, final int height, final int length)
	{
		this.width = width;
		this.height = height;
		this.length = length;

		this.boundingBox = new Region(BlockPos.ORIGIN, new BlockPos(this.width - 1, this.height - 1, this.length - 1));

		this.createMarkedPositions();
	}

	private int getFilterId(final BlockFilter filter)
	{
		int id = -1;

		for (int i = 0; i < this.filterInstances.length; i++)
		{
			final BlockFilter f = this.filterInstances[i];

			if (f != null && filter.hashCode() == f.hashCode())
			{
				id = i;
				break;
			}
		}

		return id;
	}

	private void checkForFilterAndAdd(final BlockFilter filter)
	{
		if (this.filterInstances == null)
		{
			this.filterInstances = new BlockFilter[0];
		}

		boolean hasFilter = false;

		for (final BlockFilter f : this.filterInstances)
		{
			if (f != null && filter.hashCode() == f.hashCode())
			{
				hasFilter = true;
				break;
			}
		}

		if (!hasFilter)
		{
			this.filterInstances = Arrays.copyOf(this.filterInstances, this.filterInstances.length + 1);

			this.filterInstances[this.filterInstances.length - 1] = filter;
		}
	}

	private void createMarkedPositions()
	{
		this.volume = this.width * this.height * this.length;

		this.markedPositions = new int[this.getVolume()];
		Arrays.fill(this.markedPositions, -1);
	}

	@Override
	public int getVolume()
	{
		return this.volume;
	}

	private int getIndex(final int x, final int y, final int z, final boolean throwException)
	{
		final int index = z + y * this.length + x * this.height * this.length;

		if (index < this.getVolume() && index >= 0)
		{
			return index;
		}

		if (throwException)
		{
			throw new ArrayIndexOutOfBoundsException("Tried to access position that's not in this FilterRecord: " + x + ", " + y + ", " + z);
		}

		return -1;
	}

	public int getZ(final int index)
	{
		return index / (this.width * this.length);
	}

	public int getY(int index)
	{
		final int z = this.getZ(index);
		index -= (z * this.width * this.length);

		return index / this.width;
	}

	public int getX(int index)
	{
		final int z = this.getZ(index);
		index -= (z * this.width * this.length);

		return index % this.width;
	}

	@Override
	public void listen(final IPositionRecordListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	@Override
	public boolean unlisten(final IPositionRecordListener listener)
	{
		return this.listeners.remove(listener);
	}

	@Override
	public boolean contains(final int index)
	{
		return this.markedPositions[index] != -1;
	}

	@Override
	public BlockFilter[] getData()
	{
		if (this.filterInstances == null)
		{
			this.filterInstances = new BlockFilter[0];
		}

		return this.filterInstances;
	}

	@Override
	public BlockFilter get(final int index)
	{
		final int id = this.markedPositions[index];

		if (id == -1)
		{
			return null;
		}

		return this.filterInstances[id];
	}

	@Override
	public BlockFilter get(final int x, final int y, final int z)
	{
		final int index = this.getIndex(x, y, z, true);

		return this.get(index);
	}

	@Override
	public void markPos(final BlockFilter filter, final int x, final int y, final int z)
	{
		this.checkForFilterAndAdd(filter);

		final int index = this.getIndex(x, y, z, true);

		this.markedPositions[index] = this.getFilterId(filter);

		this.listeners.forEach(l -> l.onMarkPos(filter, x, y, z));
	}

	@Override
	public void unmarkPos(final int x, final int y, final int z)
	{
		final int index = this.getIndex(x, y, z, true);

		this.markedPositions[index] = -1;

		this.listeners.forEach(l -> l.onUnmarkPos(x, y, z));
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> getPositions(final BlockFilter filter)
	{
		return new Iterable<BlockPos.MutableBlockPos>()
		{

			@Override
			public Iterator<BlockPos.MutableBlockPos> iterator()
			{
				final Iterator<BlockPos.MutableBlockPos> iter = FilterRecord.this.getBoundingBox().createShapeData().iterator();

				return new AbstractIterator<BlockPos.MutableBlockPos>()
				{

					@Override
					protected BlockPos.MutableBlockPos computeNext()
					{
						while (iter.hasNext())
						{
							final BlockPos.MutableBlockPos next = iter.next();

							if (filter.equals(FilterRecord.this.get(next.getX(), next.getY(), next.getZ())))
							{
								return next;
							}
						}

						return this.endOfData();
					}
				};
			}
		};
	}

	@Override
	public Iterable<BlockPos.MutableBlockPos> createShapeData()
	{
		return new Iterable<BlockPos.MutableBlockPos>()
		{

			@Override
			public Iterator<BlockPos.MutableBlockPos> iterator()
			{
				final Iterator<BlockPos.MutableBlockPos> iter = FilterRecord.this.getBoundingBox().createShapeData().iterator();

				return new AbstractIterator<BlockPos.MutableBlockPos>()
				{

					@Override
					protected BlockPos.MutableBlockPos computeNext()
					{
						while (iter.hasNext())
						{
							final BlockPos.MutableBlockPos next = iter.next();

							if (FilterRecord.this.contains(next.getX(), next.getY(), next.getZ()))
							{
								return next;
							}
						}

						return this.endOfData();
					}
				};
			}
		};
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

	@Override
	public IShape rotate(final Rotation rotation, final IRegion in)
	{
		return this;
	}

	@Override
	public IShape translate(final int x, final int y, final int z)
	{
		return new FilterRecord(this.width, this.height, this.length);
	}

	@Override
	public IShape translate(final BlockPos pos)
	{
		return this.translate(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.boundingBox;
	}

	@Override
	public boolean contains(final int x, final int y, final int z)
	{
		final int index = this.getIndex(x, y, z, false);

		if (index == -1)
		{
			return false;
		}

		return this.markedPositions[index] != -1;
	}

	@Override
	public boolean contains(final BlockPos pos)
	{
		return this.contains(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("width", this.width);
		tag.setInteger("height", this.height);
		tag.setInteger("length", this.length);

		tag.setInteger("volume", this.volume);

		funnel.set("boundingBox", this.boundingBox);

		funnel.setArray("filterInstances", this.filterInstances);
		funnel.setIntArray("markedPositions", this.markedPositions);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.width = tag.getInteger("width");
		this.height = tag.getInteger("height");
		this.length = tag.getInteger("length");

		this.volume = tag.getInteger("volume");

		this.boundingBox = funnel.get("boundingBox");

		this.filterInstances = funnel.getArray("filterInstances", BlockFilter.class);
		this.markedPositions = funnel.getIntArray("markedPositions");
	}

	@Override
	public int getWidth()
	{
		return this.width;
	}

	@Override
	public int getHeight()
	{
		return this.height;
	}

	@Override
	public int getLength()
	{
		return this.length;
	}
}
