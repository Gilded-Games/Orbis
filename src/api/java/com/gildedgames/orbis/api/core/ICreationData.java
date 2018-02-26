package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public interface ICreationData extends NBT
{

	ICreationData pos(BlockPos pos);

	ICreationData world(World world);

	ICreationData rotation(Rotation rotation);

	ICreationData rand(Random random);

	ICreationData creator(EntityPlayer creator);

	ICreationData placesAir(boolean placeAir);

	ICreationData schedules(boolean schedules);

	/**
	 * Should return the centered position if
	 * this creation data returns true on isCentered()
	 * @return The position we're creating at.
	 */
	BlockPos getPos();

	World getWorld();

	Random getRandom();

	Rotation getRotation();

	@Nullable
	EntityPlayer getCreator();

	boolean placeAir();

	boolean schedules();

	ICreationData clone();

	boolean shouldCreate(BlockData data, BlockPos pos);

}
