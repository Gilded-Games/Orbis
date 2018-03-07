package com.gildedgames.orbis.api;

import com.gildedgames.orbis.api.util.io.IClassSerializer;
import com.gildedgames.orbis.api.util.io.IClassSerializerRegistry;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.util.mc.NBTHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class IOHelper implements IClassSerializerRegistry
{

	public BiMap<String, IClassSerializer> idToSerializer = HashBiMap.create();

	public IOHelper()
	{

	}

	public NBTTagCompound write(final NBT nbt)
	{
		return NBTHelper.write(nbt);
	}

	public <T extends NBT> T read(final NBTTagCompound tag)
	{
		return NBTHelper.read(tag);
	}

	public <T extends NBT> T read(final World world, final NBTTagCompound tag)
	{
		return NBTHelper.read(world, tag);
	}

	@Override
	public void register(IClassSerializer serializer)
	{
		this.idToSerializer.put(serializer.identifier(), serializer);
	}

	@Override
	public IClassSerializer findSerializer(String id)
	{
		return this.idToSerializer.get(id);
	}

	@Override
	public IClassSerializer findSerializer(NBT nbt)
	{
		return this.findSerializer(nbt.getClass());
	}

	@Override
	public IClassSerializer findSerializer(Class<? extends NBT> clazz)
	{
		for (IClassSerializer s : this.idToSerializer.values())
		{
			if (s.isRegistered(clazz))
			{
				return s;
			}
		}

		throw new RuntimeException("This object has not been registered to a serializer: " + clazz);
	}

	@Override
	public String findID(IClassSerializer serializer)
	{
		return this.idToSerializer.inverse().get(serializer);
	}
}
