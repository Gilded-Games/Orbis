package com.gildedgames.orbis.api.data.blueprint;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.IDataHolder;
import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.management.IDataMetadata;
import com.gildedgames.orbis.api.data.management.impl.DataMetadata;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Random;

public class BlueprintStackerData implements IData, IDataHolder<BlockDataContainer>
{
	public static final String EXTENSION = "blueprintstacker";

	private IDataHolder<BlueprintData> top, bottom;

	private IDataHolder<BlueprintData>[] segments;

	private IDataMetadata metadata;

	private IDimensions largestDim;

	private BlueprintStackerData()
	{
		this.metadata = new DataMetadata();
	}

	public BlueprintStackerData(IDataHolder<BlueprintData> top, IDataHolder<BlueprintData> bottom, IDataHolder<BlueprintData>[] segments)
	{
		this();

		this.top = top;
		this.bottom = bottom;
		this.segments = segments;

		this.evaluateLargestDim();
	}

	public IDataHolder<BlueprintData> getBottom()
	{
		return this.bottom;
	}

	public IDataHolder<BlueprintData> getTop()
	{
		return this.top;
	}

	public IDataHolder<BlueprintData>[] getSegments()
	{
		return this.segments;
	}

	private void evaluateLargestDim()
	{
		int width = Integer.MIN_VALUE;
		int height = 0;
		int length = Integer.MIN_VALUE;

		height += this.top.getLargestHeight();
		height += this.bottom.getLargestHeight();

		for (IDataHolder<BlueprintData> holder : this.segments)
		{
			width = Math.max(width, holder.getLargestWidth());
			length = Math.max(length, holder.getLargestLength());
		}

		width = Math.max(width, this.top.getLargestWidth());
		length = Math.max(length, this.top.getLargestLength());

		width = Math.max(width, this.bottom.getLargestWidth());
		length = Math.max(length, this.bottom.getLargestLength());

		this.largestDim = new Region(BlockPos.ORIGIN, new BlockPos(width - 1, height - 1, length - 1));
	}

	@Override
	public void preSaveToDisk(IWorldObject object)
	{

	}

	@Override
	public IData clone()
	{
		return new BlueprintStackerData(this.top, this.bottom, this.segments);
	}

	@Override
	public String getFileExtension()
	{
		return BlueprintStackerData.EXTENSION;
	}

	@Override
	public IDataMetadata getMetadata()
	{
		return this.metadata;
	}

	@Override
	public void readMetadataOnly(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.metadata = funnel.get("metadata");
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("metadata", this.metadata);
		funnel.set("top", this.top);
		funnel.set("bottom", this.bottom);
		funnel.setArray("segments", this.segments);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.metadata = funnel.get("metadata");
		this.top = funnel.get("top");
		this.bottom = funnel.get("bottom");
		this.segments = funnel.getArray("segments", IDataHolder.class);

		this.evaluateLargestDim();
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.metadata.getIdentifier());

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj instanceof BlueprintStackerData)
		{
			final BlueprintStackerData o = (BlueprintStackerData) obj;

			final EqualsBuilder builder = new EqualsBuilder();

			builder.append(this.metadata.getIdentifier(), o.metadata.getIdentifier());

			return builder.isEquals();
		}

		return false;
	}

	@Override
	public BlockDataContainer get(World world, Random random)
	{
		return this.get(world, random, random.nextInt(this.segments.length + 1));
	}

	public BlockDataContainer get(World world, Random random, int segmentIndexHeight)
	{
		BlueprintData chosenTop = this.top.get(world, random);
		BlueprintData chosenBottom = this.bottom.get(world, random);

		int height = 0;

		height += chosenTop.getHeight();
		height += chosenBottom.getHeight();

		BlueprintData[] chosenSegments = new BlueprintData[segmentIndexHeight];

		int j = 0;

		for (int i = this.segments.length - 1; i >= this.segments.length - segmentIndexHeight; i--)
		{
			IDataHolder<BlueprintData> holder = this.segments[i];

			BlueprintData chosen = holder.get(world, random);

			height += chosen.getHeight();

			chosenSegments[j++] = chosen;
		}

		BlockDataContainer result = new BlockDataContainer(this.getLargestWidth(), height, this.getLargestLength());

		this.funnelInto(result, chosenBottom.getBlockDataContainer(), (this.getLargestWidth() - chosenBottom.getWidth()) / 2, 0,
				(this.getLargestLength() - chosenBottom.getLength()) / 2);

		int itHeight = chosenBottom.getHeight();

		for (int i = 0; i < chosenSegments.length; i++)
		{
			BlueprintData segment = chosenSegments[i];

			this.funnelInto(result, segment.getBlockDataContainer(), (this.getLargestWidth() - segment.getWidth()) / 2, itHeight,
					(this.getLargestLength() - segment.getLength()) / 2);

			itHeight += segment.getHeight();
		}

		this.funnelInto(result, chosenTop.getBlockDataContainer(), (this.getLargestWidth() - chosenTop.getWidth()) / 2, itHeight,
				(this.getLargestLength() - chosenTop.getLength()) / 2);

		return result;
	}

	private void funnelInto(BlockDataContainer into, BlockDataContainer from, int xOffset, int yOffset, int zOffset)
	{
		for (int x = 0; x < from.getWidth(); x++)
		{
			for (int y = 0; y < from.getHeight(); y++)
			{
				for (int z = 0; z < from.getLength(); z++)
				{
					BlockData block = from.get(x, y, z);

					into.set(block, x + xOffset, y + yOffset, z + zOffset);
				}
			}
		}
	}

	@Override
	public int getLargestHeight()
	{
		return this.largestDim.getHeight();
	}

	@Override
	public int getLargestWidth()
	{
		return this.largestDim.getWidth();
	}

	@Override
	public int getLargestLength()
	{
		return this.largestDim.getLength();
	}
}
