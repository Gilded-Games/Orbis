package com.gildedgames.orbis.common.world_objects;

import com.gildedgames.orbis.client.renderers.RenderBlueprintEditing;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis_api.core.tree.INode;
import com.gildedgames.orbis_api.core.tree.INodeTreeListener;
import com.gildedgames.orbis_api.core.tree.LayerLink;
import com.gildedgames.orbis_api.core.world_objects.BlueprintRegion;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintVariable;
import com.gildedgames.orbis_api.data.blueprint.IBlueprintDataListener;
import com.gildedgames.orbis_api.data.pathway.Entrance;
import com.gildedgames.orbis_api.data.region.IColored;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.schedules.*;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.util.io.NBTFunnel;
import com.gildedgames.orbis_api.util.mc.NBT;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Blueprint extends BlueprintRegion implements IWorldObject, IColored, IBlueprintDataListener,
		IBlueprint, IScheduleRecordListener, INodeTreeListener<IScheduleLayer, LayerLink>
{
	private final List<IScheduleLayerHolderListener> listeners = Lists.newArrayList();

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private World world;

	private IWorldRenderer renderer;

	private int currentScheduleLayer;

	private boolean isDirty;

	private Blueprint()
	{
		super();
	}

	private Blueprint(final World world)
	{
		super(world);
		this.world = world;
	}

	public Blueprint(final World world, final IRegion region)
	{
		super(region);
		this.world = world;
		this.setBounds(region);
		this.data.getScheduleLayerTree().listen(this);
		this.data.getVariableTree().listen(new BlueprintVariableTreeListener(this));
	}

	public Blueprint(final World world, final BlockPos pos, final BlueprintData data)
	{
		super(pos, data);
		this.world = world;
		this.data.listen(this);
		this.data.getScheduleLayerTree().listen(this);
		this.data.getVariableTree().listen(new BlueprintVariableTreeListener(this));
	}

	public Blueprint(final World world, final BlockPos pos, final Rotation rotation, final BlueprintData data)
	{
		super(pos, rotation, data);
		this.world = world;
		this.data.listen(this);
		this.data.getScheduleLayerTree().listen(this);
		this.data.getVariableTree().listen(new BlueprintVariableTreeListener(this));
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

	public Entrance findIntersectingEntrance(IShape s)
	{
		for (Entrance e : this.getData().entrances())
		{
			int minX = e.getBounds().getMin().getX() + this.getPos().getX();
			int minY = e.getBounds().getMin().getY() + this.getPos().getY();
			int minZ = e.getBounds().getMin().getZ() + this.getPos().getZ();

			int maxX = minX + e.getBounds().getWidth() - 1;
			int maxY = minY + e.getBounds().getHeight() - 1;
			int maxZ = minZ + e.getBounds().getLength() - 1;

			IRegion r = s.getBoundingBox();

			if (maxX >= r.getBoundingBox().getMin().getX() && minX <= r.getMax().getX() && maxY >= r.getMin().getY() && minY <= r.getMax().getY()
					&& maxZ >= r.getMin().getZ() && minZ <= r.getMax().getZ())
			{
				return e;
			}
		}

		return null;
	}

	public Entrance findIntersectingEntrance(BlockPos pos)
	{
		for (Entrance e : this.getData().entrances())
		{
			int minX = e.getBounds().getMin().getX() + this.getPos().getX();
			int minY = e.getBounds().getMin().getY() + this.getPos().getY();
			int minZ = e.getBounds().getMin().getZ() + this.getPos().getZ();

			int maxX = minX + e.getBounds().getWidth() - 1;
			int maxY = minY + e.getBounds().getHeight() - 1;
			int maxZ = minZ + e.getBounds().getLength() - 1;

			if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ)
			{
				return e;
			}
		}

		return null;
	}

	public Optional<ISchedule> findIntersectingSchedule(BlockPos pos)
	{
		if (this.getCurrentScheduleLayerNode() == null)
		{
			return Optional.empty();
		}

		return RegionHelp.findIntersecting(this.getCurrentScheduleLayerNode().getData().getScheduleRecord().getSchedules(ISchedule.class),this.getPos(),pos);
	}

	private void checkScheduleLayerExists()
	{
		boolean exists = this.getData().getScheduleLayerTree().containsId(this.currentScheduleLayer);

		if (!exists && !this.getData().getScheduleLayerTree().isEmpty())
		{
			this.currentScheduleLayer = Collections.min(this.getData().getScheduleLayerTree().getInternalMap().keySet());
		}
	}

	@Override
	public int getCurrentScheduleLayerIndex()
	{
		this.checkScheduleLayerExists();

		return this.currentScheduleLayer;
	}

	/**
	 * @param index The index of the current layer from the
	 *             	internal BlueprintData object.
	 */
	@Override
	public void setCurrentScheduleLayerIndex(final int index)
	{
		INode<IScheduleLayer, LayerLink> newNode = this.getData().getScheduleLayerTree().get(index);

		if (newNode == null)
		{
			return;
		}

		int oldIndex = this.currentScheduleLayer;
		INode<IScheduleLayer, LayerLink> oldNode = this.getData().getScheduleLayerTree().get(oldIndex);
		IScheduleLayer oldLayer = oldNode.getData();

		if (oldLayer != null)
		{
			oldLayer.getScheduleRecord().unlisten(this);
		}

		this.currentScheduleLayer = index;

		this.getCurrentScheduleLayerNode().getData().getScheduleRecord().listen(this);

		Lock w = this.lock.readLock();
		w.lock();

		try
		{
			this.listeners.forEach(l -> l.onChangeScheduleLayerNode(oldNode, oldIndex, this.getCurrentScheduleLayerNode(), this.currentScheduleLayer));
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public INode<IScheduleLayer, LayerLink> getCurrentScheduleLayerNode()
	{
		this.checkScheduleLayerExists();

		return this.getData().getScheduleLayerTree().get(this.currentScheduleLayer);
	}

	@Override
	public BlueprintData getData()
	{
		return this.data;
	}

	@Override
	public void onUpdate()
	{

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
	public void markDirty()
	{
		this.isDirty = true;
	}

	@Override
	public void markClean()
	{
		this.isDirty = false;
	}

	@Override
	public boolean isDirty()
	{
		return this.isDirty;
	}

	@Override
	public World getWorld()
	{
		return this.world;
	}

	@Override
	public void setWorld(World world)
	{
		this.world = world;
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
			this.renderer = new RenderBlueprintEditing(this);
		}

		return this.renderer;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		super.write(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("currentScheduleLayer", this.currentScheduleLayer);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		super.read(tag);

		NBTFunnel funnel = new NBTFunnel(tag);

		this.data.listen(this);

		this.currentScheduleLayer = tag.getInteger("currentScheduleLayer");

		this.data.getScheduleLayerTree().listen(this);
		this.data.getVariableTree().listen(new BlueprintVariableTreeListener(this));

		this.data.getScheduleLayerTree().getNodes().forEach(l -> l.getData().getScheduleRecord().listen(this));
	}

	@Override
	public int getColor()
	{
		return 0x99B6FF;
	}

	@Override
	public void onDataChanged()
	{
		this.markDirty();
	}

	@Override
	public void onAddSchedule(ISchedule schedule)
	{
		this.markDirty();
	}

	@Override
	public void onRemoveSchedule(ISchedule schedule)
	{
		this.markDirty();
	}

	@Override
	public void onAddEntrance(Entrance entrance)
	{
		this.markDirty();
	}

	@Override
	public void onRemoveEntrance(Entrance entrance)
	{
		this.markDirty();
	}

	@Override
	public void onSetData(INode<IScheduleLayer, LayerLink> node, IScheduleLayer iScheduleLayer, int id)
	{
		node.getData().getScheduleRecord().listen(this);

		this.markDirty();
	}

	@Override
	public void onPut(INode<IScheduleLayer, LayerLink> node, int id)
	{
		node.getData().getScheduleRecord().listen(this);

		this.markDirty();
	}

	@Override
	public void onRemove(INode<IScheduleLayer, LayerLink> node, int id)
	{
		node.getData().getScheduleRecord().unlisten(this);

		this.markDirty();
	}

	private static class BlueprintVariableTreeListener implements INodeTreeListener<BlueprintVariable, NBT>
	{
		private Blueprint blueprint;

		public BlueprintVariableTreeListener(Blueprint blueprint)
		{
			this.blueprint = blueprint;
		}

		@Override
		public void onSetData(INode<BlueprintVariable, NBT> node, BlueprintVariable variable, int id)
		{
			this.blueprint.markDirty();
		}

		@Override
		public void onPut(INode<BlueprintVariable, NBT> node, int id)
		{
			this.blueprint.markDirty();
		}

		@Override
		public void onRemove(INode<BlueprintVariable, NBT> node, int id)
		{
			this.blueprint.markDirty();
		}
	}
}
