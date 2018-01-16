package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.IBlueprintDataListener;
import com.gildedgames.orbis.api.data.region.*;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolder;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayerHolderListener;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.RotationHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.RenderBlueprintEditing;
import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.OrbisCore;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Blueprint extends AbstractRegion implements IWorldObject, IMutableRegion, IRotateable, IColored, IBlueprintDataListener,
		IScheduleLayerHolder
{
	private final World world;

	private final List<IWorldObjectGroup> trackedGroups = Lists.newArrayList();

	private final List<IScheduleLayerHolderListener> listeners = Lists.newArrayList();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	protected Rotation rotation = Rotation.NONE;

	private IWorldRenderer renderer;

	private BlueprintData data;

	private BlockPos min = BlockPos.ORIGIN, max = BlockPos.ORIGIN;

	private int currentScheduleLayer;

	private Blueprint(final World world)
	{
		this.world = world;
	}

	public Blueprint(final World world, final IRegion region)
	{
		this.world = world;
		this.data = new BlueprintData(region);

		this.setBounds(region);

		this.data.listen(this);
	}

	public Blueprint(final World world, final BlockPos pos, final BlueprintData data)
	{
		this.world = world;
		this.data = data;

		this.setPos(pos);

		this.data.listen(this);
	}

	public Blueprint(final World world, final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		this.world = world;
		this.data = data;
		this.rotation = rotation;

		this.setPos(pos);

		this.data.listen(this);
	}

	@Override
	public void listen(final IScheduleLayerHolderListener listener)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			if (!this.listeners.contains(listener))
			{
				this.listeners.add(listener);
			}
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public boolean unlisten(final IScheduleLayerHolderListener listener)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			return this.listeners.remove(listener);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public int getCurrentScheduleLayerIndex()
	{
		return this.currentScheduleLayer;
	}

	/**
	 * @param index The index of the current layer from the
	 *             	internal BlueprintData object.
	 */
	@Override
	public void setCurrentScheduleLayerIndex(final int index)
	{
		int oldIndex = this.currentScheduleLayer;
		IScheduleLayer oldLayer = this.getData().getScheduleLayers().get(oldIndex);

		this.currentScheduleLayer = index;

		Lock w = this.lock.readLock();
		w.lock();

		try
		{
			this.listeners.forEach(l -> l.onChangeScheduleLayer(oldLayer, oldIndex, this.getCurrentScheduleLayer(), this.currentScheduleLayer));
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public IScheduleLayer getCurrentScheduleLayer()
	{
		return this.getData().getScheduleLayers().get(this.currentScheduleLayer);
	}

	@Override
	public BlueprintData getData()
	{
		return this.data;
	}

	@Override
	public Rotation getRotation()
	{
		return this.rotation;
	}

	public BlockData getBlock(final BlockPos pos)
	{
		final BlockPos transformed = this.transformForBlueprint(pos);
		return this.getBlockDataContainer().get(transformed);
	}

	public BlockPos transformForBlueprint(final BlockPos pos)
	{
		final Rotation transformRot =
				this.rotation == Rotation.CLOCKWISE_90 ?
						Rotation.COUNTERCLOCKWISE_90 :
						this.rotation == Rotation.COUNTERCLOCKWISE_90 ? Rotation.CLOCKWISE_90 : this.rotation;
		final BlockPos rotated = RotationHelp.rotate(pos, this, transformRot);
		final IRegion rotatedRegion = RotationHelp.rotate(this, transformRot);
		return new BlockPos(rotated.getX() - rotatedRegion.getMin().getX(), rotated.getY() - rotatedRegion.getMin().getY(),
				rotated.getZ() - rotatedRegion.getMin().getZ());
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.min.hashCode());

		return builder.toHashCode();
	}

	@Override
	public boolean equals(final Object o)
	{
		final boolean flag = super.equals(o);

		if (flag)
		{
			return true;
		}

		if (!(o instanceof Blueprint))
		{
			return false;
		}

		final Blueprint b = (Blueprint) o;

		if (this.getMin().getX() == b.getMin().getX() && this.getMax().getX() == b.getMax().getX() && this.getMin().getY() == b.getMin().getY()
				&& this.getMax().getY() == b.getMax().getY() && this.getMin().getZ() == b.getMin().getZ() && this.getMax().getZ() == b.getMax()
				.getZ() && this.data == b.data)
		{
			return this.getWorld().equals(b.getWorld());
		}

		return false;
	}

	@Override
	public int getWidth()
	{
		return this.data.getWidth();
	}

	@Override
	public int getHeight()
	{
		return this.data.getHeight();
	}

	@Override
	public int getLength()
	{
		return this.data.getLength();
	}

	@Override
	public void trackGroup(final IWorldObjectGroup group)
	{
		if (!this.trackedGroups.contains(group))
		{
			this.trackedGroups.add(group);
		}
	}

	@Override
	public void untrackGroup(final IWorldObjectGroup group)
	{
		this.trackedGroups.remove(group);
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public BlockPos getPos()
	{
		return this.min;
	}

	@Override
	public void setPos(final BlockPos pos)
	{
		this.min = pos;
		this.max = RegionHelp.getMax(this.min, this.getWidth(), this.getHeight(), this.getLength());

		this.notifyDataChange();
	}

	@Override
	public IShape getShape()
	{
		return this;
	}

	@Override
	public IWorldRenderer getRenderer()
	{
		if (OrbisCore.isClient() && this.renderer == null)
		{
			final RenderShape r = new RenderShape(this);

			r.useCustomColors = true;

			r.colorGrid = this.getColor();
			r.colorBorder = this.getColor();

			this.renderer = r;

			final Lock w = this.renderer.getSubRenderersLock().writeLock();
			w.lock();

			try
			{
				this.renderer.getSubRenderers(this.getWorld()).add(new RenderBlueprintEditing(this));
			}
			finally
			{
				w.unlock();
			}
		}

		return this.renderer;
	}

	@Override
	public BlockPos getMin()
	{
		return this.min;
	}

	@Override
	public BlockPos getMax()
	{
		return this.max;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setPos("min", this.min);

		tag.setString("rotation", this.rotation.name());

		funnel.set("state", this.data);

		tag.setInteger("currentScheduleLayer", this.currentScheduleLayer);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.min = funnel.getPos("min");

		this.rotation = Rotation.valueOf(tag.getString("rotation"));

		this.data = funnel.get("state");

		this.max = RegionHelp.getMax(this.min, this.getWidth(), this.getHeight(), this.getLength());

		this.notifyDataChange();

		this.data.listen(this);

		this.currentScheduleLayer = tag.getInteger("currentScheduleLayer");
	}

	@Override
	public void setBounds(final IRegion region)
	{
		this.min = region.getMin();
		this.max = region.getMax();

		this.notifyDataChange();
	}

	@Override
	public void setBounds(final BlockPos corner1, final BlockPos corner2)
	{
		this.min = RegionHelp.getMin(corner1, corner2);
		this.max = RegionHelp.getMax(corner1, corner2);

		this.notifyDataChange();
	}

	public BlockDataContainer getBlockDataContainer()
	{
		return this.data.getBlockDataContainer();
	}

	@Override
	public int getColor()
	{
		return 0x99B6FF;
	}

	@Override
	public void onRemoveScheduleLayer(final IScheduleLayer layer, final int index)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onAddScheduleLayer(final IScheduleLayer layer, final int index)
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}

	@Override
	public void onDataChanged()
	{
		this.trackedGroups.forEach(IWorldObjectGroup::markDirty);
	}
}
