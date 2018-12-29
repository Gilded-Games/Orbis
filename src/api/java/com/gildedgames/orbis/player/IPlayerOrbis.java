package com.gildedgames.orbis.player;

import com.gildedgames.orbis.player.modules.ISelectionTypesModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Orbis extended player capability that manages all Orbis-related additions
 * and states.
 */
public interface IPlayerOrbis
{
	ISelectionTypesModule selectionTypes();

	void onUpdate(LivingEvent.LivingUpdateEvent event);

	World getWorld();

	/**
	 * @return The {@link EntityPlayer} entity this capability belongs to.
	 */
	EntityPlayer getEntity();

	/**
	 * Writes this capability to {@param tag}.
	 * @param tag The {@link NBTTagCompound} to write to
	 */
	void write(NBTTagCompound tag);

	/**
	 * Updates this capability from {@param tag}.
	 * @param tag The {@link NBTTagCompound} to read from
	 */
	void read(NBTTagCompound tag);
}
