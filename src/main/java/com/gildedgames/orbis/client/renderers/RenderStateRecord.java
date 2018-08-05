package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.schedules.IBlueprint;
import com.gildedgames.orbis_api.data.schedules.IPositionRecord;
import com.gildedgames.orbis_api.data.schedules.IPositionRecordListener;
import com.gildedgames.orbis_api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderStateRecord implements IWorldRenderer, IPositionRecordListener<IBlockState>, IFocusedRender
{
	private final IPositionRecord<IBlockState> stateRecord;

	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final Map<BlockPos, IWorldRenderer> chunkToRenderer = Maps.newHashMap();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final IWorldObject parentObject;

	private List<BlockPos> pendingChunksToUpdate = Lists.newArrayList();

	private boolean disabled;

	private boolean rotateData;

	private IScheduleLayer layer;

	private IBlueprint blueprint;

	public RenderStateRecord(IBlueprint blueprint, IScheduleLayer layer, final IPositionRecord<IBlockState> stateRecord, final IWorldObject parentObject,
			boolean rotateData)
	{
		this.layer = layer;
		this.stateRecord = stateRecord;
		this.parentObject = parentObject;
		this.rotateData = rotateData;
		this.blueprint = blueprint;

		this.stateRecord.listen(this);

		for (int chunkX = 0; chunkX <= stateRecord.getWidth() / 16; chunkX++)
		{
			for (int chunkY = 0; chunkY <= stateRecord.getHeight() / 16; chunkY++)
			{
				for (int chunkZ = 0; chunkZ <= stateRecord.getLength() / 16; chunkZ++)
				{
					BlockPos pos = new BlockPos(chunkX, chunkY, chunkZ);

					this.pendingChunksToUpdate.add(pos);
				}
			}
		}

		for (BlockPos pos : this.pendingChunksToUpdate)
		{
			this.updateChunk(pos.getX(), pos.getY(), pos.getZ());
		}

		this.pendingChunksToUpdate.clear();
	}

	@Override
	public void setFocused(boolean focused)
	{
		for (IWorldRenderer r : this.subRenderers)
		{
			if (r instanceof IFocusedRender)
			{
				IFocusedRender c = (IFocusedRender) r;

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
		return this.stateRecord;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.parentObject.getShape().getBoundingBox();
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
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
	public void preRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void preRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{
	}

	@Override
	public void postRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

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
		boolean isFocused = false;

		if (this.chunkToRenderer.containsKey(chunkPos))
		{
			IWorldRenderer toRemove = this.chunkToRenderer.get(chunkPos);

			if (toRemove instanceof RenderStateRecordChunk)
			{
				isFocused = ((RenderStateRecordChunk) toRemove).isFocused();
			}

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

		final RenderStateRecordChunk chunk = new RenderStateRecordChunk(this.blueprint, this.layer, this.stateRecord, this.parentObject, chunkPos,
				this.rotateData);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			chunk.setFocused(isFocused);
			this.subRenderers.add(chunk);
		}
		finally
		{
			w.unlock();
		}

		this.chunkToRenderer.put(chunkPos, chunk);
	}

	@Override
	public void onMarkPos(final IBlockState state, final int x, final int y, final int z)
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
}
