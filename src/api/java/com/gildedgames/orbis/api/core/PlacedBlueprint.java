package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.core.util.BlueprintUtil;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.ScheduleRegion;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlacedBlueprint implements NBT
{
	private final World world;

	private BlockDataChunk[] chunks;

	private Map<ChunkPos, List<PlacedEntity>> placedEntities = Maps.newHashMap();

	private BlueprintDefinition def;

	private int definitionID;

	private String registryId;

	private ICreationData data;

	private boolean hasGeneratedAChunk;

	private List<ScheduleRegion> scheduleRegions = Lists.newArrayList();

	public PlacedBlueprint(final World world, final BlueprintDefinition def, final ICreationData data)
	{
		this.world = world;
		this.def = def;

		this.registryId = def.getRegistry().getRegistryId();
		this.definitionID = def.getRegistry().getID(this.def);

		this.data = data;

		this.bakeChunks();
		this.placeEntities();
		this.bakeScheduleRegions();
	}

	public PlacedBlueprint(final World world, final NBTTagCompound tag)
	{
		this.world = world;

		this.read(tag);

		this.bakeChunks();
		this.placeEntities();
		this.bakeScheduleRegions();
	}

	public List<ScheduleRegion> getScheduleRegions()
	{
		return this.scheduleRegions;
	}

	public ScheduleRegion getScheduleFromTriggerID(String triggerId)
	{
		for (ScheduleRegion s : this.scheduleRegions)
		{
			if (s.getTriggerID().equals(triggerId))
			{
				return s;
			}
		}

		return null;
	}

	private void bakeScheduleRegions()
	{
		this.def.getData().getSchedules(ScheduleRegion.class).forEach(s ->
		{
			ScheduleRegion c = NBTHelper.clone(s);

			RegionHelp.translate(c.getBounds(), this.getCreationData().getPos().getX(), this.getCreationData().getPos().getY(),
					this.getCreationData().getPos().getZ());

			this.scheduleRegions.add(c);
		});
	}

	private void placeEntities()
	{
		for (ScheduleRegion s : this.def.getData().getSchedules(ScheduleRegion.class))
		{
			for (int i = 0; i < s.getSpawnEggsInventory().getSizeInventory(); i++)
			{
				ItemStack stack = s.getSpawnEggsInventory().getStackInSlot(i);

				if (stack.getItem() instanceof ItemMonsterPlacer)
				{
					BlockPos pos = this.getCreationData().getPos().add(s.getBounds().getMin());
					pos.add(this.world.rand.nextInt(s.getBounds().getWidth()), 0, this.world.rand.nextInt(s.getBounds().getHeight()));

					PlacedEntity placedEntity = new PlacedEntity(stack, pos);

					ChunkPos p = new ChunkPos(this.getCreationData().getPos().getX() >> 4, this.getCreationData().getPos().getZ() >> 4);

					if (!this.placedEntities.containsKey(p))
					{
						this.placedEntities.put(p, Lists.newArrayList());
					}

					this.placedEntities.get(p).add(placedEntity);
				}
			}
		}
	}

	private void bakeChunks()
	{
		final BlockDataContainer blocks = this.def.getData().getBlockDataContainer().clone();

		for (IScheduleLayer layer : this.getDef().getData().getScheduleLayers().values())
		{
			for (BlockFilter filter : layer.getDataRecord().getData())
			{
				filter.apply(layer.getDataRecord().getPositions(filter), blocks, this.data);
			}
		}

		final ChunkPos[] chunksOccupied = BlueprintUtil.getChunksInsideTemplate(this.getDef().getData(), this.getCreationData());

		this.chunks = new BlockDataChunk[chunksOccupied.length];

		final BlockPos min = this.data.getPos();

		final Region region = new Region(new BlockPos(0, 0, 0), new BlockPos(blocks.getWidth() - 1, blocks.getHeight() - 1, blocks.getLength() - 1));

		final int startChunkX = min.getX() >> 4;
		final int startChunkZ = min.getZ() >> 4;

		int xDif = min.getX() % 16;
		int zDif = min.getZ() % 16;

		if (xDif < 0)
		{
			xDif = 16 - Math.abs(xDif);
		}

		if (zDif < 0)
		{
			zDif = 16 - Math.abs(zDif);
		}

		for (final BlockPos.MutableBlockPos iterPos : region.getMutableBlockPosInRegion())
		{
			final int chunkX = ((min.getX() + iterPos.getX()) >> 4) - startChunkX;
			final int chunkZ = ((min.getZ() + iterPos.getZ()) >> 4) - startChunkZ;

			int index = 0;

			for (int i = 0; i < chunksOccupied.length; i++)
			{
				final ChunkPos p = chunksOccupied[i];

				if (p.x - startChunkX == chunkX && p.z - startChunkZ == chunkZ)
				{
					if (this.chunks[i] == null)
					{
						this.chunks[i] = new BlockDataChunk(p, new BlockDataContainer(16, blocks.getHeight(), 16));
					}

					index = i;
					break;
				}
			}

			final BlockDataChunk chunk = this.chunks[index];

			final BlockData block = blocks.get(iterPos.getX(), iterPos.getY(), iterPos.getZ());

			if (chunk != null && block != null)
			{
				chunk.getContainer()
						.set(block, (iterPos.getX() + xDif) % 16, iterPos.getY(), (iterPos.getZ() + zDif) % 16);
			}
		}
	}

	public Map<ChunkPos, List<PlacedEntity>> getPlacedEntities()
	{
		return this.placedEntities;
	}

	public BlueprintDefinition getDef()
	{
		return this.def;
	}

	public ICreationData getCreationData()
	{
		return this.data;
	}

	public BlockDataChunk[] getDataChunks()
	{
		return this.chunks;
	}

	public void markGeneratedAChunk()
	{
		this.hasGeneratedAChunk = true;
	}

	public boolean hasGeneratedAChunk()
	{
		return this.hasGeneratedAChunk;
	}

	@Override
	public boolean equals(final Object obj)
	{
		boolean flag = false;

		if (obj == this)
		{
			flag = true;
		}
		else if (obj instanceof PlacedBlueprint)
		{
			final PlacedBlueprint o = (PlacedBlueprint) obj;
			final EqualsBuilder builder = new EqualsBuilder();

			builder.append(this.definitionID, o.definitionID);
			builder.append(this.data, o.data);
			builder.append(this.hasGeneratedAChunk, o.hasGeneratedAChunk);

			flag = builder.isEquals();
		}

		return flag;
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.definitionID);
		builder.append(this.data);
		builder.append(this.hasGeneratedAChunk);

		return builder.toHashCode();
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("registryId", this.registryId);
		tag.setInteger("definitionId", this.definitionID);

		funnel.set("creation", this.data);

		tag.setBoolean("hasGeneratedAChunk", this.hasGeneratedAChunk);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.def = OrbisAPI.services().findDefinitionRegistry(tag.getString("registryId")).get(tag.getInteger("definitionId"));

		this.data = funnel.get(this.world, "creation");

		this.hasGeneratedAChunk = tag.getBoolean("hasGeneratedAChunk");
	}

	@Override
	public PlacedBlueprint clone()
	{
		final PlacedBlueprint clone = new PlacedBlueprint(this.world, this.def, this.data.clone());

		clone.chunks = Arrays.copyOf(this.chunks, this.chunks.length);
		clone.hasGeneratedAChunk = this.hasGeneratedAChunk;

		return clone;
	}
}
