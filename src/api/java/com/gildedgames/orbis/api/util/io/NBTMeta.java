package com.gildedgames.orbis.api.util.io;

import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.nbt.NBTTagCompound;

public interface NBTMeta extends NBT
{
	/**
	 * Reads this object's meta from a {@link NBTTagCompound}
	 * @param tag The tag to write to
	 */
	void readMetadataOnly(NBTTagCompound tag);
}
