package com.gildedgames.orbis.api.util.mc;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.util.io.IClassSerializer;
import com.google.common.collect.AbstractIterator;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class NBTHelper
{

	public static NBTTagList getTagList(final NBTTagCompound tag, final String key)
	{
		return tag.getTagList(key, 10);
	}

	public static Iterable<NBTTagCompound> getIterator(final NBTTagCompound tag, final String tagListKey)
	{
		return getIterator(getTagList(tag, tagListKey));
	}

	/**
	 * Get the iterator for a taglist in an NBTTagCompound.
	 * Simply a nice shortcut method.
	 */
	public static Iterable<NBTTagCompound> getIterator(final NBTTagList tagList)
	{
		return new Iterable<NBTTagCompound>()
		{
			@Override
			public Iterator<NBTTagCompound> iterator()
			{
				return new AbstractIterator<NBTTagCompound>()
				{
					private int i = 0;

					@Override
					protected NBTTagCompound computeNext()
					{
						if (this.i >= tagList.tagCount())
						{
							return this.endOfData();
						}

						final NBTTagCompound tag = tagList.getCompoundTagAt(this.i);
						this.i++;
						return tag;
					}
				};
			}
		};
	}

	public static NBTTagCompound readNBTFromFile(final String fileName)
	{
		return readNBTFromFile(new File(DimensionManager.getCurrentSaveRootDirectory(), fileName));
	}

	public static NBTTagCompound readNBTFromFile(final File file)
	{
		try
		{
			if (!file.exists())
			{
				return null;
			}
			final FileInputStream inputStream = new FileInputStream(file);
			return CompressedStreamTools.readCompressed(inputStream);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void writeNBTToFile(final NBTTagCompound tag, final String fileName)
	{
		writeNBTToFile(tag, new File(DimensionManager.getCurrentSaveRootDirectory(), fileName));
	}

	public static void writeNBTToFile(final NBTTagCompound tag, final File file)
	{
		file.mkdirs();
		final File tmpFile = new File(file.getParentFile(), file.getName() + ".tmp");
		try
		{
			CompressedStreamTools.writeCompressed(tag, new FileOutputStream(tmpFile));
			if (file.exists())
			{
				file.delete();
			}
			tmpFile.renameTo(file);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	public static BlockPos readBlockPos(final NBTTagCompound tag)
	{
		if (tag == null || tag.getBoolean("_null") || !tag.hasKey("_null"))
		{
			return null;
		}

		return new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
	}

	public static NBTBase writeBlockPos(final BlockPos pos)
	{
		final NBTTagCompound tag = new NBTTagCompound();

		if (pos == null)
		{
			tag.setBoolean("_null", true);

			return tag;
		}

		tag.setBoolean("_null", false);

		tag.setInteger("x", pos.getX());
		tag.setInteger("y", pos.getY());
		tag.setInteger("z", pos.getZ());

		return tag;
	}

	public static ChunkPos readChunkPos(final NBTTagCompound tag)
	{
		if (tag == null || tag.getBoolean("_null") || !tag.hasKey("_null"))
		{
			return null;
		}

		return new ChunkPos(tag.getInteger("x"), tag.getInteger("z"));
	}

	public static NBTBase writeChunkPos(final ChunkPos pos)
	{
		final NBTTagCompound tag = new NBTTagCompound();

		if (pos == null)
		{
			tag.setBoolean("_null", true);

			return tag;
		}

		tag.setBoolean("_null", false);

		tag.setInteger("x", pos.x);
		tag.setInteger("z", pos.z);

		return tag;
	}

	public static double[] readDoubleArray(final NBTTagCompound tag)
	{
		if (tag == null || (tag.hasKey("_null") && tag.getBoolean("_null")))
		{
			return null;
		}

		final double[] array = new double[tag.getInteger("length")];

		for (int i = 0; i < array.length; i++)
		{
			array[i] = tag.getDouble(String.valueOf(i));
		}

		return array;
	}

	public static NBTBase writeDoubleArray(final double[] array)
	{
		final NBTTagCompound tag = new NBTTagCompound();

		if (array == null)
		{
			tag.setBoolean("_null", true);

			return tag;
		}

		tag.setBoolean("_null", false);
		tag.setInteger("length", array.length);

		int i = 0;

		for (final double value : array)
		{
			tag.setDouble(String.valueOf(i), value);

			i++;
		}

		return tag;
	}

	/**
	 * Writes the nbt data without any serialization
	 * @param nbt
	 * @return
	 */
	public static NBTTagCompound writeRaw(final NBT nbt)
	{
		final NBTTagCompound tag = new NBTTagCompound();

		nbt.write(tag);

		return tag;
	}

	public static <T extends NBT> T clone(T nbt)
	{
		return clone(null, nbt);
	}

	public static <T extends NBT> T clone(World world, T nbt)
	{
		NBTTagCompound tag = new NBTTagCompound();

		nbt.write(tag);

		IClassSerializer serializer = OrbisAPI.services().io().findSerializer(nbt);

		final int id = serializer.serialize(nbt);
		final T obj = serializer.deserialize(world, id);

		obj.read(tag);

		return obj;
	}

	public static NBTTagCompound write(final NBT nbt)
	{
		final NBTTagCompound tag = new NBTTagCompound();

		if (nbt == null)
		{
			tag.setBoolean("_null", true);

			return tag;
		}

		IClassSerializer serializer = OrbisAPI.services().io().findSerializer(nbt);

		tag.setBoolean("_null", false);
		tag.setString("s_id", serializer.identifier());
		tag.setInteger("id", serializer.serialize(nbt));

		final NBTTagCompound data = new NBTTagCompound();

		nbt.write(data);

		tag.setTag("data", data);

		return tag;
	}

	public static <T extends NBT> T read(final World world, final NBTTagCompound tag)
	{
		if (tag == null || tag.getBoolean("_null") || !tag.hasKey("_null"))
		{
			return null;
		}

		IClassSerializer serializer = OrbisAPI.services().io().findSerializer(tag.getString("s_id"));

		final int id = tag.getInteger("id");

		final T obj = serializer.deserialize(world, id);
		obj.read(tag.getCompoundTag("data"));

		return obj;
	}

	public static <T extends NBT> T read(final NBTTagCompound tag)
	{
		if (tag == null || tag.getBoolean("_null") || !tag.hasKey("_null"))
		{
			return null;
		}

		IClassSerializer serializer = OrbisAPI.services().io().findSerializer(tag.getString("s_id"));

		final int id = tag.getInteger("id");

		final T obj = serializer.deserialize(id);
		obj.read(tag.getCompoundTag("data"));

		return obj;
	}

	public static <T extends NBT> T loadWithoutReading(final NBTTagCompound tag)
	{
		if (tag == null || tag.getBoolean("_null") || !tag.hasKey("_null"))
		{
			return null;
		}

		IClassSerializer serializer = OrbisAPI.services().io().findSerializer(tag.getString("s_id"));

		final int id = tag.getInteger("id");

		return serializer.deserialize(id);
	}
}
