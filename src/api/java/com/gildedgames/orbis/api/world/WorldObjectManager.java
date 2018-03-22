package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.util.Collection;
import java.util.List;

/**
 * TODO: IMPLEMENT CACHING for the object groups
 * with a file persisisting cache. Will drastically
 * improve performance and prevent any issues with
 * OOM exceptions.
 */
public class WorldObjectManager extends WorldSavedData
{
	private static final String DATA_NAME = "Orbis_WorldObjectManager";

	private final List<IWorldObjectManagerObserver> observers = Lists.newArrayList();

	private int dimension;

	private World world;

	private BiMap<Integer, IWorldObject> idToObject = HashBiMap.create();

	private int nextId;

	public WorldObjectManager()
	{
		super(DATA_NAME);
	}

	public WorldObjectManager(final String s)
	{
		super(s);
	}

	public WorldObjectManager(final World world)
	{
		super(DATA_NAME);

		this.world = world;
	}

	public static WorldObjectManager get(final World world)
	{
		World using;

		if (world.isRemote)
		{
			if (Minecraft.getMinecraft().isIntegratedServerRunning())
			{
				using = DimensionManager.getWorld(world.provider.getDimension());

				if (using == null)
				{
					using = world;
				}
			}
			else
			{
				using = world;
			}
		}
		else
		{
			using = world.getMinecraftServer().getWorld(world.provider.getDimension());
		}

		final MapStorage storage = using.getPerWorldStorage();
		WorldObjectManager instance = (WorldObjectManager) storage.getOrLoadData(WorldObjectManager.class, DATA_NAME);

		if (instance == null)
		{
			instance = new WorldObjectManager(world);
			storage.setData(DATA_NAME, instance);
		}

		return instance;
	}

	/**
	 * Should be called when an observer is added to
	 * this manager
	 */
	private void refreshObserver(final IWorldObjectManagerObserver observer)
	{
		observer.onReloaded(this);
	}

	public World getWorld()
	{
		return this.world;
	}

	public void setWorld(final World world)
	{
		this.world = world;
	}

	public void updateObjects()
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			obj.onUpdate();
		}
	}

	public void checkForDirtyObjects()
	{
		for (final IWorldObject obj : this.idToObject.values())
		{
			if (obj.isDirty())
			{
				this.markDirty();
				break;
			}
		}

		if (this.isDirty())
		{
			for (final IWorldObject obj : this.idToObject.values())
			{
				if (obj.isDirty())
				{
					obj.markClean();
				}
			}
		}
	}

	public <T extends IWorldObject> boolean hasObject(final T object)
	{
		return this.idToObject.inverse().containsKey(object);
	}

	public <T extends IWorldObject> int getID(final T object)
	{
		if (object == null)
		{
			throw new NullPointerException();
		}

		BiMap<IWorldObject, Integer> inverse = this.idToObject.inverse();

		return inverse.get(object);
	}

	public <T extends IWorldObject> T getObject(final int id)
	{
		return (T) this.idToObject.get(id);
	}

	public <T extends IWorldObject> void setObject(final int id, final T object)
	{
		this.idToObject.put(id, object);

		for (final IWorldObjectManagerObserver observer : this.observers)
		{
			observer.onObjectAdded(this, object);
		}

		this.markDirty();
	}

	public <T extends IWorldObject> int addObject(final T object)
	{
		final int id = this.fetchNextId();

		this.setObject(id, object);

		return id;
	}

	public int fetchNextId()
	{
		return this.nextId++;
	}

	public <T extends IWorldObject> boolean removeObject(final T object)
	{
		return this.removeObject(this.getID(object));
	}

	public boolean removeObject(final int id)
	{
		final IWorldObject object = this.idToObject.get(id);

		if (this.idToObject.containsKey(id))
		{
			this.idToObject.remove(id);

			for (final IWorldObjectManagerObserver observer : this.observers)
			{
				observer.onObjectRemoved(this, object);
			}

			this.markDirty();
		}

		return object != null;
	}

	public Collection<IWorldObject> getObjects()
	{
		return this.idToObject.values();
	}

	public void addObserver(final IWorldObjectManagerObserver observer)
	{
		this.observers.add(observer);

		this.refreshObserver(observer);
	}

	public boolean removeObserver(final IWorldObjectManagerObserver observer)
	{
		return this.observers.remove(observer);
	}

	public boolean containsObserver(final IWorldObjectManagerObserver observer)
	{
		return this.observers.contains(observer);
	}

	@Override
	public void readFromNBT(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.nextId = tag.getInteger("nextId");
		this.dimension = tag.getInteger("dimension");

		if (this.world == null)
		{
			this.world = DimensionManager.getWorld(this.dimension);
		}

		this.idToObject = HashBiMap.create(funnel.getIntMap(this.world, "objects"));

		for (final IWorldObjectManagerObserver observer : this.observers)
		{
			observer.onReloaded(this);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("nextId", this.nextId);
		tag.setInteger("dimension", this.dimension);

		funnel.setIntMap("objects", this.idToObject);

		return tag;
	}

}
