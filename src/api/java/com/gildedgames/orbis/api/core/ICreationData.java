package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.block.BlockData;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public interface ICreationData<SELF extends ICreationData> extends NBT
{

	SELF placesVoid(boolean placesVoid);

	SELF pos(BlockPos pos);

	SELF world(World world);

	SELF rotation(Rotation rotation);

	SELF seed(long seed);

	SELF creator(EntityPlayer creator);

	SELF placesAir(boolean placeAir);

	SELF schedules(boolean schedules);

	/**
	 * Should return the centered position if
	 * this creation data returns true on isCentered()
	 * @return The position we're creating at.
	 */
	BlockPos getPos();

	//TODO: Might need to remove this since World cannot be reliably serialized
	//TODO: Alternatively, make it so it stores a dim id and constantly tries to fetch it
	World getWorld();

	Random getRandom();

	Rotation getRotation();

	@Nullable
	EntityPlayer getCreator();

	boolean placeAir();

	boolean schedules();

	boolean placesVoid();

	ICreationData clone();

	boolean shouldCreate(BlockData data, BlockPos pos);

}
