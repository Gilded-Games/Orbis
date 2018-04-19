package com.gildedgames.orbis.api.block;

import com.gildedgames.orbis.api.core.ICreationData;
import com.gildedgames.orbis.api.data.DataCondition;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.data.schedules.IFilterOptions;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolder;
import com.gildedgames.orbis.api.processing.BlockAccessBlockDataContainer;
import com.gildedgames.orbis.api.processing.BlockAccessExtendedWrapper;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.processing.IBlockAccessExtended;
import com.gildedgames.orbis.api.util.OrbisTuple;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.WorldObjectUtils;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockFilterLayer implements NBT
{

	private static final List<IBlockState> AIR_BLOCKS = Lists.newArrayList(Blocks.AIR.getDefaultState());

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

		return replacementBlock.getBlockState();
	}

	public void apply(Iterable<BlockPos.MutableBlockPos> positions, BlockDataContainer container, ICreationData creationData, IFilterOptions options)
	{
		World world = creationData.getWorld();

		if (this.condition == null)
		{
			this.condition = new DataCondition();
		}

		if (!this.condition.isMet(creationData.getRandom(), world) || this.replacementBlocks.isEmpty())
		{
			return;
		}

		IBlockAccessExtended access = new BlockAccessBlockDataContainer(world, container);

		final DataPrimer primer = new DataPrimer(access);

		BlockDataWithConditions replacementBlock = null;

		if (!options.choosesPerBlock())
		{
			replacementBlock = this.getRandom(creationData.getRandom(), world);
		}

		for (final BlockPos.MutableBlockPos pos : positions)
		{
			final IBlockState state;

			state = access.getBlockState(pos);

			if (!this.getFilterType().filter(state, this.requiredBlocks, world, creationData.getRandom()))
			{
				continue;
			}

			if (options.choosesPerBlock())
			{
				replacementBlock = this.getRandom(creationData.getRandom(), world);
			}

			if (pos.getY() >= 256 || replacementBlock == null || !replacementBlock.getReplaceCondition().isMet(creationData.getRandom(), world))
			{
				continue;
			}

			if (!creationData.shouldCreate(replacementBlock, pos))
			{
				continue;
			}

			primer.create(replacementBlock, pos.toImmutable(), creationData);
		}
	}

	/**
	 * Applies this layer to a shape
	 */
	public void apply(IRegion relocateTo, final BlockFilter parentFilter, IShape shape, final ICreationData creationData, IFilterOptions options)
	{
		World world = creationData.getWorld();

		if (this.condition == null)
		{
			this.condition = new DataCondition();
		}

		if (!this.condition.isMet(creationData.getRandom(), world) || this.replacementBlocks.isEmpty())
		{
			return;
		}

		IShape intersect = null;
		IScheduleLayerHolder holder = null;

		if (creationData.schedules())
		{
			intersect = WorldObjectUtils.getIntersectingShape(world, shape);

			if (intersect instanceof IScheduleLayerHolder)
			{
				holder = (IScheduleLayerHolder) intersect;
			}
		}

		final DataPrimer primer = new DataPrimer(new BlockAccessExtendedWrapper(world));

		BlockDataWithConditions replacementBlock = null;

		if (!options.choosesPerBlock())
		{
			replacementBlock = this.getRandom(creationData.getRandom(), world);
		}

		final BlockPos min = creationData.getPos();
		BlockPos max = new BlockPos(min.getX() + shape.getBoundingBox().getWidth() - 1, min.getY() + shape.getBoundingBox().getHeight() - 1,
				min.getZ() + shape.getBoundingBox().getLength() - 1);

		final int rotAmount = Math.abs(RotationHelp.getRotationAmount(creationData.getRotation(), Rotation.NONE));

		if (rotAmount != 0)
		{
			for (final OrbisTuple<BlockPos.MutableBlockPos, BlockPos.MutableBlockPos> tuple : RotationHelp
					.getAllInBoxRotated(min, max, creationData.getRotation(), relocateTo))
			{
				final BlockPos.MutableBlockPos beforeRot = tuple.getFirst();
				BlockPos.MutableBlockPos rotated = tuple.getSecond();

				if (shape.contains(beforeRot))
				{
					this.applyInner(primer, rotated, replacementBlock, intersect, holder, parentFilter, shape, creationData, options);
				}
			}
		}
		else
		{
			for (final BlockPos.MutableBlockPos iterPos : shape.getShapeData())
			{
				this.applyInner(primer, iterPos, replacementBlock, intersect, holder, parentFilter, shape, creationData, options);
			}
		}
	}

	private void applyInner(DataPrimer primer, BlockPos p, BlockDataWithConditions replacementBlock, IShape intersect, IScheduleLayerHolder holder,
			final BlockFilter parentFilter, IShape boundingBox, final ICreationData creationData, IFilterOptions options)
	{
		World world = creationData.getWorld();

		BlockPos without = p.add(-creationData.getPos().getX(), -creationData.getPos().getY(), -creationData.getPos().getZ());

		int schedX = 0;
		int schedY = 0;
		int schedZ = 0;

		if (holder != null)
		{
			schedX = without.getX() - intersect.getBoundingBox().getMin().getX();
			schedY = without.getY() - intersect.getBoundingBox().getMin().getY();
			schedZ = without.getZ() - intersect.getBoundingBox().getMin().getZ();
		}

		final IBlockState state;

		if (!creationData.schedules())
		{
			state = world.getBlockState(without);

			if (!this.getFilterType().filter(state, this.requiredBlocks, world, creationData.getRandom()))
			{
				return;
			}
		}

		if (options.choosesPerBlock())
		{
			replacementBlock = this.getRandom(creationData.getRandom(), world);
		}

		if (without.getY() >= 256 || replacementBlock == null || !replacementBlock.getReplaceCondition().isMet(creationData.getRandom(), world))
		{
			return;
		}

		if (!creationData.shouldCreate(replacementBlock, without))
		{
			return;
		}

		if (creationData.schedules() && holder != null)
		{
			BlockFilter posFilter = holder.getCurrentScheduleLayer().getFilterRecord().get(schedX, schedY, schedZ);

			boolean found = false;

			if (posFilter != null)
			{
				for (BlockFilterLayer layer : parentFilter.getFilters())
				{
					if (layer.getFilterType() == BlockFilterType.ALL || layer.getRequiredBlocks()
							.equals(posFilter.getFilters().get(0).getReplacementBlocks()))
					{
						found = true;
						break;
					}
				}
			}
			else
			{
				for (BlockFilterLayer layer : parentFilter.getFilters())
				{
					if (layer.getFilterType() == BlockFilterType.ALL || layer.getRequiredBlocks().equals(AIR_BLOCKS))
					{
						found = true;
						break;
					}
				}
			}

			if (!found)
			{
				return;
			}

			if (creationData.getRandom().nextFloat() > options.getEdgeNoise())
			{
				if (replacementBlock.isAir())
				{
					holder.getCurrentScheduleLayer().getFilterRecord().unmarkPos(schedX, schedY, schedZ);
				}
				else
				{
					holder.getCurrentScheduleLayer().getFilterRecord().markPos(parentFilter, schedX, schedY, schedZ);
				}
			}
		}
		else
		{
			BlockPos c = p.toImmutable();

			boolean edge = !boundingBox.contains(c.getX(), c.getY() + 1, c.getZ()) || !boundingBox.contains(c.getX(), c.getY() - 1, c.getZ())
					|| !boundingBox
					.contains(c.getX() + 1, c.getY(), c.getZ()) || !boundingBox.contains(c.getX() - 1, c.getY(), c.getZ()) || !boundingBox
					.contains(c.getX(), c.getY(), c.getZ() + 1) || !boundingBox.contains(c.getX(), c.getY(), c.getZ() - 1);

			if (!edge || creationData.getRandom().nextFloat() > options.getEdgeNoise())
			{
				primer.create(replacementBlock, c, creationData);
			}
		}

		// TODO: Re-enable event
		/*final ChangeBlockEvent blockEvent = new ChangeBlockEvent(world, pos, options.getCreator());
		MinecraftForge.EVENT_BUS.post(blockEvent);*/
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

