package com.gildedgames.orbis.api.block;

import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.DataCondition;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.IScheduleLayerHolder;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockFilterLayer implements NBT
{

	public boolean chooseBlockPerBlock = true;

	protected List<BlockDataWithConditions> requiredBlocks = Lists.newArrayList();

	protected List<BlockDataWithConditions> replacementBlocks = Lists.newArrayList();

	protected String name = "";

	protected DataCondition condition;

	private BlockFilterType blockFilterType = BlockFilterType.ALL;

	public BlockFilterLayer()
	{
		super();
		this.condition = new DataCondition();
	}

	public BlockFilterLayer(final List<BlockDataWithConditions> requiredBlocks, final List<BlockDataWithConditions> newBlocks)
	{
		this();
		this.requiredBlocks = Lists.newArrayList(requiredBlocks);
		this.replacementBlocks = Lists.newArrayList(newBlocks);
	}

	/**
	 * Gets the list of blocks that trigger the filter
	 */
	public List<BlockDataWithConditions> getRequiredBlocks()
	{
		return this.requiredBlocks;
	}

	/**
	 * Sets the list of blocks that trigger the filter
	 */
	public void setRequiredBlocks(final BlockDataWithConditions... requiredBlocks)
	{
		this.requiredBlocks = Lists.newArrayList(Arrays.asList(requiredBlocks));
	}

	/**
	 * Sets the list of blocks that trigger the filter
	 */
	public void setRequiredBlocks(final List<BlockDataWithConditions> requiredBlocks)
	{
		this.requiredBlocks = Lists.newArrayList(requiredBlocks);
	}

	public List<BlockDataWithConditions> getReplacementBlocks()
	{
		return this.replacementBlocks;
	}

	public void setReplacementBlocks(final BlockDataWithConditions... newBlocks)
	{
		this.replacementBlocks = Lists.newArrayList(Arrays.asList(newBlocks));
	}

	public void setReplacementBlocks(final List<BlockDataWithConditions> newBlocks)
	{
		this.replacementBlocks = newBlocks;
	}

	public BlockFilterType getFilterType()
	{
		return this.blockFilterType;
	}

	public void setFilterType(final BlockFilterType blockFilterType)
	{
		this.blockFilterType = blockFilterType;
	}

	private BlockDataWithConditions getRandom(final Random random, final World world)
	{
		final float randomValue = random.nextFloat() * this.totalBlockChance();
		float chanceSum = 0.0f;

		for (final BlockDataWithConditions block : this.replacementBlocks)
		{
			if (block.getReplaceCondition().isMet(randomValue, chanceSum, random, world))
			{
				return block;
			}

			chanceSum += block.getReplaceCondition().getWeight();
		}

		return null;
	}

	public IBlockState getSample(final World world, final Random rand, final IBlockState state)
	{
		final BlockDataWithConditions replacementBlock = this.getRandom(rand, world);

		if (!this.getFilterType().filter(state, this.requiredBlocks, world, rand))
		{
			return Blocks.AIR.getDefaultState();
		}

		return replacementBlock.getBlockState();
	}

	/**
	 * Applies this layer to a shape
	 */
	public void apply(final BlockFilter parentFilter, final IShape shape, final World world, final ICreationData options)
	{
		if (this.condition == null)
		{
			this.condition = new DataCondition();
		}

		if (!this.condition.isMet(options.getRandom(), world) || this.replacementBlocks.isEmpty())
		{
			return;
		}

		IShape intersect = null;
		IScheduleLayerHolder holder = null;

		if (options.schedules())
		{
			final WorldObjectManager manager = WorldObjectManager.get(world);
			final IWorldObjectGroup group = manager.getGroup(0);

			intersect = group.getIntersectingShape(shape);

			if (intersect instanceof IScheduleLayerHolder)
			{
				holder = (IScheduleLayerHolder) intersect;
			}
		}

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		BlockDataWithConditions replacementBlock = null;

		if (!this.chooseBlockPerBlock)
		{
			replacementBlock = this.getRandom(options.getRandom(), world);
		}

		for (final BlockPos.MutableBlockPos pos : shape.createShapeData())
		{
			int schedX = 0;
			int schedY = 0;
			int schedZ = 0;

			if (holder != null)
			{
				schedX = pos.getX() - intersect.getBoundingBox().getMin().getX();
				schedY = pos.getY() - intersect.getBoundingBox().getMin().getY();
				schedZ = pos.getZ() - intersect.getBoundingBox().getMin().getZ();
			}

			final IBlockState state;

			if (!options.schedules())
			{
				state = world.getBlockState(pos);

				if (!this.getFilterType().filter(state, this.requiredBlocks, world, options.getRandom()))
				{
					continue;
				}
			}

			if (this.chooseBlockPerBlock)
			{
				replacementBlock = this.getRandom(options.getRandom(), world);
			}

			if (pos.getY() >= 256 || replacementBlock == null || !replacementBlock.getReplaceCondition().isMet(options.getRandom(), world))
			{
				continue;
			}

			if (!options.shouldCreate(replacementBlock, pos))
			{
				continue;
			}

			if (options.schedules() && holder != null)
			{
				if (options.erases())
				{
					holder.getCurrentScheduleLayer().getDataRecord().unmarkPos(schedX, schedY, schedZ);
				}
				else
				{
					holder.getCurrentScheduleLayer().getDataRecord().markPos(parentFilter, schedX, schedY, schedZ);
				}
			}
			else
			{
				primer.create(replacementBlock, pos.toImmutable(), options);
			}

			// TODO: Re-enable event
			/*final ChangeBlockEvent blockEvent = new ChangeBlockEvent(world, pos, options.getCreator());
			MinecraftForge.EVENT_BUS.post(blockEvent);*/
		}
	}

	public float totalBlockChance()
	{
		float total = 0f;

		for (final BlockDataWithConditions BlockDataFilter : this.replacementBlocks)
		{
			total += BlockDataFilter.getReplaceCondition().getWeight();
		}

		return total;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		tag.setString("name", this.name);

		tag.setString("filterName", this.getFilterType().name());

		tag.setBoolean("chooseBlockPerBlock", this.chooseBlockPerBlock);

		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.set("condition", this.condition);

		funnel.setList("requiredBlocks", this.requiredBlocks);
		funnel.setList("replacementBlocks", this.replacementBlocks);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		this.name = tag.getString("name");

		this.blockFilterType = BlockFilterType.valueOf(tag.getString("filterName"));

		this.chooseBlockPerBlock = tag.getBoolean("chooseBlockPerBlock");

		final NBTFunnel funnel = new NBTFunnel(tag);

		this.condition = funnel.get("condition");

		this.requiredBlocks = funnel.getList("requiredBlocks");
		this.replacementBlocks = funnel.getList("replacementBlocks");
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.getReplacementBlocks());
		builder.append(this.getRequiredBlocks());
		builder.append(this.getFilterType());

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (!(obj instanceof BlockFilterLayer))
		{
			return false;
		}

		final BlockFilterLayer layer = (BlockFilterLayer) obj;

		final EqualsBuilder builder = new EqualsBuilder();

		builder.append(this.getReplacementBlocks(), layer.getReplacementBlocks());
		builder.append(this.getRequiredBlocks(), layer.getRequiredBlocks());
		builder.append(this.getFilterType(), layer.getFilterType());

		return builder.isEquals();
	}

}

