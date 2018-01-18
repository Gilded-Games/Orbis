package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.api.block.BlockFilter;
import com.gildedgames.orbis.api.data.region.IRegion;
import com.gildedgames.orbis.api.data.schedules.*;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderFilterRecord implements IWorldRenderer, IPositionRecordListener<BlockFilter>, IScheduleLayerHolderListener
{
	private final IPositionRecord<BlockFilter> positionRecord;

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final Map<BlockPos, IWorldRenderer> chunkToRenderer = Maps.newHashMap();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final IWorldObject parentObject;

	private List<BlockPos> pendingChunksToUpdate = Lists.newArrayList();

	private boolean disabled;

	private IScheduleLayerHolder layerHolder;

	public RenderFilterRecord(final IPositionRecord<BlockFilter> positionRecord, IScheduleLayerHolder holder, final IWorldObject parentObject)
	{
		this.positionRecord = positionRecord;
		this.parentObject = parentObject;

		this.layerHolder = holder;

		this.positionRecord.listen(this);

		for (int chunkX = 0; chunkX <= positionRecord.getWidth() / 16; chunkX++)
		{
			for (int chunkY = 0; chunkY <= positionRecord.getHeight() / 16; chunkY++)
			{
				for (int chunkZ = 0; chunkZ <= positionRecord.getLength() / 16; chunkZ++)
				{
					BlockPos pos = new BlockPos(chunkX, chunkY, chunkZ);

					this.pendingChunksToUpdate.add(pos);
				}
			}
		}
	}

	public void setFocused(boolean focused)
	{
		for (IWorldRenderer r : this.subRenderers)
		{
			if (r instanceof RenderFilterRecordChunk)
			{
				RenderFilterRecordChunk c = (RenderFilterRecordChunk) r;

				c.setFocused(focused);
			}
		}
	}

	@Override
	public boolean isDisabled()
	{
		return this.disabled;
	}

	@Override
	public void setDisabled(final boolean disabled)
	{
		this.disabled = disabled;
	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.positionRecord;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.parentObject.getShape().getBoundingBox();
	}

	@Override
	public void render(final World world, final float partialTicks)
	{
		if (this.layerHolder != null)
		{
			this.layerHolder.listen(this);
			this.layerHolder = null;
		}

		Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			for (BlockPos pos : this.pendingChunksToUpdate)
			{
				this.updateChunk(pos.getX(), pos.getY(), pos.getZ());
			}

			this.pendingChunksToUpdate.clear();
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public List<IWorldRenderer> getSubRenderers(final World world)
	{
		return this.subRenderers;
	}

	@Override
	public ReadWriteLock getSubRenderersLock()
	{
		return this.lock;
	}

	@Override
	public void onRemoved()
	{

	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	private void updateChunk(int chunkX, int chunkY, int chunkZ)
	{
		BlockPos chunkPos = new BlockPos(chunkX, chunkY, chunkZ);

		if (this.chunkToRenderer.containsKey(chunkPos))
		{
			IWorldRenderer toRemove = this.chunkToRenderer.get(chunkPos);

			final Lock w = this.lock.writeLock();
			w.lock();

			try
			{
				this.subRenderers.remove(toRemove);
			}
			finally
			{
				w.unlock();
			}
		}

		final RenderFilterRecordChunk chunk = new RenderFilterRecordChunk(this.positionRecord, this.parentObject, chunkPos);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			chunk.setFocused(true);
			this.subRenderers.add(chunk);
		}
		finally
		{
			w.unlock();
		}

		this.chunkToRenderer.put(chunkPos, chunk);
	}

	@Override
	public void onMarkPos(final BlockFilter filter, final int x, final int y, final int z)
	{
		BlockPos pos = new BlockPos(x / 16, y / 16, z / 16);

		Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			if (!this.pendingChunksToUpdate.contains(pos))
			{
				this.pendingChunksToUpdate.add(pos);
			}
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onUnmarkPos(final int x, final int y, final int z)
	{
		BlockPos pos = new BlockPos(x / 16, y / 16, z / 16);

		Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			if (!this.pendingChunksToUpdate.contains(pos))
			{
				this.pendingChunksToUpdate.add(pos);
			}
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onChangeScheduleLayer(IScheduleLayer prevLayer, int prevIndex, IScheduleLayer newLayer, int newIndex)
	{

	}
}
