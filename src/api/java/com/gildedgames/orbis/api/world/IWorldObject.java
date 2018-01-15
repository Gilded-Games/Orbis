package com.gildedgames.orbis.api.world;

import com.gildedgames.orbis.api.data.management.IData;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.util.mc.NBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The most basic object the world can hold
 * Each object has a position and a renderer
 */
public interface IWorldObject extends NBT
{

	/**
	 * Tracks the group internally. This means that
	 * this object is attached to this group in some way.
	 * @param group The group that this object is attached to.
	 */
	void trackGroup(IWorldObjectGroup group);

	/**
	 * Untracks the group internally, meaning this object
	 * is no longer attached to that group.
	 * @param group The group that was once attached to this object.
	 */
	void untrackGroup(IWorldObjectGroup group);

	World getWorld();

	BlockPos getPos();

	void setPos(BlockPos pos);

	IShape getShape();

	@SideOnly(Side.CLIENT)
	IWorldRenderer getRenderer();

	IData getData();

}
