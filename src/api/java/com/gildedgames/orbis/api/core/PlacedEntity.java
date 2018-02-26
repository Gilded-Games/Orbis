package com.gildedgames.orbis.api.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class PlacedEntity
{
	private ItemStack egg;

	private BlockPos pos;

	public PlacedEntity(ItemStack egg, BlockPos pos)
	{
		this.egg = egg;
		this.pos = pos;
	}

	public ItemStack getEgg()
	{
		return this.egg;
	}

	public BlockPos getPos()
	{
		return this.pos;
	}

	public void spawn(World world)
	{
		Entity entity = ItemMonsterPlacer
				.spawnCreature(world, ItemMonsterPlacer.getNamedIdFrom(this.egg), (double) this.pos.getX() + 0.5D, (double) this.pos.getY(),
						(double) this.pos.getZ() + 0.5D);

		if (entity != null)
		{
			if (entity instanceof EntityLivingBase && this.egg.hasDisplayName())
			{
				entity.setCustomNameTag(this.egg.getDisplayName());
			}

			MinecraftServer minecraftserver = world.getMinecraftServer();

			if (minecraftserver != null)
			{
				NBTTagCompound nbttagcompound = this.egg.getTagCompound();

				if (nbttagcompound != null && nbttagcompound.hasKey("EntityTag", 10))
				{
					if (!entity.ignoreItemEntityData())
					{
						return;
					}

					NBTTagCompound nbttagcompound1 = entity.writeToNBT(new NBTTagCompound());
					UUID uuid = entity.getUniqueID();
					nbttagcompound1.merge(nbttagcompound.getCompoundTag("EntityTag"));
					entity.setUniqueId(uuid);
					entity.readFromNBT(nbttagcompound1);
				}
			}
		}
	}
}
