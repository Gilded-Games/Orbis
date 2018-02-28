package com.gildedgames.orbis.api.block;

import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class BlockFilter implements NBT
{
	private final List<BlockFilterLayer> filters = Lists.newArrayList();

	public BlockFilter()
	{
		super();
	}

	public BlockFilter(final BlockFilterLayer layer)
	{
		super();
		this.add(layer);
	}

	public BlockFilter(final BlockFilter blockLayerContainer)
	{
		super();
		this.addAll(blockLayerContainer.getFilters());
	}

	public BlockFilter(final List<BlockFilterLayer> layers)
	{
		super();
		this.addAll(layers);
	}

	public IBlockState getSample(final World world, final Random rand, final IBlockState state)
	{
		IBlockState sample = Blocks.AIR.getDefaultState();

		for (final BlockFilterLayer layer : this.filters)
		{
			if (layer != null)
			{
				sample = layer.getSample(world, rand, state);
			}
		}

		return sample;
	}

	public void apply(Iterable<BlockPos.MutableBlockPos> positions, BlockDataContainer container, ICreationData options)
	{
		for (final BlockFilterLayer layer : this.filters)
		{
			if (layer != null)
			{
				layer.apply(positions, container, options);
			}
		}
	}

	public void apply(IShape boundingBox, Iterable<BlockPos.MutableBlockPos> positions, final ICreationData options)
	{
		for (final BlockFilterLayer layer : this.filters)
		{
			if (layer != null)
			{
				layer.apply(this, boundingBox, positions, options);
			}
		}
	}

	public void apply(final IShape shape, final ICreationData options)
	{
		for (final BlockFilterLayer layer : this.filters)
		{
			if (layer != null)
			{
				layer.apply(this, shape, options);
			}
		}
	}

	public void add(final BlockFilterLayer layer)
	{
		this.filters.add(layer);
	}

	public void addAll(final Collection<BlockFilterLayer> layers)
	{
		this.filters.addAll(layers);
	}

	public void clear()
	{
		this.filters.clear();
	}

	public List<BlockFilterLayer> getFilters()
	{
		return this.filters;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setList("filterList", this.filters);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.clear();
		final List<BlockFilterLayer> filters = funnel.getList("filterList");
		this.addAll(filters);
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder(17, 31);

		builder.append(this.filters);

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj instanceof BlockFilter)
		{
			final BlockFilter f = (BlockFilter) obj;

			final EqualsBuilder builder = new EqualsBuilder();

			builder.append(f.filters, this.filters);

			return builder.isEquals();
		}

		return false;
	}

	public boolean isEmpty()
	{
		return this.filters.isEmpty();
	}

	public BlockFilterLayer getByIndex(final int i)
	{
		return this.filters.get(i);
	}

}
