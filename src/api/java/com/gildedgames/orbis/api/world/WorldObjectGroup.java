package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class WorldObjectGroup implements IWorldObjectGroup
{
	private final List<IWorldObjectGroupObserver> observers = Lists.newArrayList();

	private BiMap<Integer, IWorldObject> idToObject = HashBiMap.create();

	private World world;

	private int nextId;

	private boolean isDirty;

	private WorldObjectGroup()
	{

	}

	public WorldObjectGroup(final World world)
	{
		this.setWorld(world);
	}

	@Override
	public boolean isDirty()
	{
		return this.isDirty;
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
	public void setWorld(final World world)
	{
		this.world = world;
	}

	@Override
	public <T extends IWorldObject> boolean hasObject(final T object)
	{
		return this.idToObject.inverse().containsKey(object);
	}

	@Override
	public <T extends IWorldObject> int getID(final T object)
	{
		if (object == null)
		{
			throw new NullPointerException();
		}

		return this.idToObject.inverse().get(object);
	}

	@Override
	public <T extends IWorldObject> T getObject(final int id)
	{
		return (T) this.idToObject.get(id);
	}

	@Override
	public <T extends IWorldObject> void setObject(final int id, final T object)
	{
		this.idToObject.put(id, object);

		for (final IWorldObjectGroupObserver observer : this.observers)
		{
			observer.onObjectAdded(this, object);
		}

		object.trackGroup(this);

		if (this.world != null)
		{
			WorldObjectManager.get(this.world).markDirty();
		}
	}

	@Override
	public <T extends IWorldObject> int addObject(final T object)
	{
		final int id = this.nextId;

		this.setObject(this.nextId++, object);

		return id;
	}

	@Override
	public <T extends IWorldObject> boolean removeObject(final T object)
	{
		return this.removeObject(this.getID(object));
	}

	@Override
	public boolean removeObject(final int id)
	{
		final IWorldObject object = this.idToObject.get(id);

		if (this.idToObject.containsKey(id))
		{
			this.idToObject.remove(id);

			for (final IWorldObjectGroupObserver observer : this.observers)
			{
				observer.onObjectRemoved(this, object);
			}

			object.untrackGroup(this);

			if (this.world != null)
			{
				WorldObjectManager.get(this.world).markDirty();
			}
		}

		return object != null;
	}

	@Override
	public Collection<IWorldObject> getObjects()
	{
		return this.idToObject.values();
	}

	/**
	 * Should be called when an observer is added to
	 * this manager
	 */
	private void refreshObserver(final IWorldObjectGroupObserver observer)
	{
		for (final IWorldObject object : this.idToObject.values())
		{
			observer.onObjectAdded(this, object);
		}
	}

	@Override
	public void addObserver(final IWorldObjectGroupObserver observer)
	{
		this.observers.add(observer);

		this.refreshObserver(observer);
	}

	@Override
	public boolean removeObserver(final IWorldObjectGroupObserver observer)
	{
		return this.observers.remove(observer);
	}

	@Override
	public boolean containsObserver(final IWorldObjectGroupObserver observer)
	{
		return this.observers.contains(observer);
	}

	@Override
	public IShape getIntersectingShape(final BlockPos pos)
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (area.contains(pos))
				{
					return area;
				}
			}
		}

		return null;
	}

	@Override
	public <T extends IShape> T getIntersectingShape(final Class<T> shapeType, final BlockPos pos)
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (area.contains(pos) && area.getClass() == shapeType)
				{
					return shapeType.cast(area);
				}
			}
		}

		return null;
	}

	@Override
	public IShape getIntersectingShape(final IShape shape)
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.intersects(area, shape))
				{
					return area;
				}
			}

		}

		return null;
	}

	@Override
	public <T extends IShape> T getIntersectingShape(final Class<T> shapeType, final IShape shape)
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.intersects(area, shape) && area.getClass() == shapeType)
				{
					return shapeType.cast(area);
				}
			}
		}

		return null;
	}

	@Override
	public <T extends IShape> List<T> getIntersectingShapes(final Class<T> shapeType, final IShape shape)
	{
		final List<T> intersecting = Lists.newArrayList();

		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.intersects(area, shape) && area.getClass() == shapeType)
				{
					intersecting.add(shapeType.cast(area));
				}
			}
		}

		return intersecting;
	}

	@Override
	public IShape getContainedShape(final IShape shape)
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.contains(area, shape))
				{
					return area;
				}
			}
		}

		return null;
	}

	@Override
	public <T extends IShape> T getContainedShape(final Class<T> shapeType, final IShape shape)
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.contains(area, shape) && area.getClass() == shapeType)
				{
					return shapeType.cast(area);
				}
			}
		}

		return null;
	}

	@Override
	public <T extends IShape> List<T> getContainedShapes(final Class<T> shapeType, final IShape shape)
	{
		final List<T> contained = Lists.newArrayList();

		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj instanceof IShape)
			{
				final IShape area = (IShape) obj;

				if (RegionHelp.contains(shape, area))
				{
					if (area.getClass() == shapeType)
					{
						contained.add(shapeType.cast(area));
					}
				}
			}
		}

		return contained;
	}

	@Override
	public boolean isIntersectingShapes(final IShape shape)
	{
		return this.getIntersectingShape(shape) != null;
	}

	@Override
	public boolean isContainedInShape(final IShape shape)
	{
		return this.getContainedShape(shape) != null;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("nextId", this.nextId);

		funnel.setIntMap("idToObject", this.idToObject);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.nextId = tag.getInteger("nextId");

		this.idToObject = HashBiMap.create(funnel.getIntMap(this.world, "idToObject"));

		this.idToObject.values().forEach(o -> o.trackGroup(this));
	}
}
