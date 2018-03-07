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

	void markClean();

	boolean isDirty();

	World getWorld();

	BlockPos getPos();

	void setPos(BlockPos pos);

	IShape getShape();

	@SideOnly(Side.CLIENT)
	IWorldRenderer getRenderer();

	IData getData();

}
