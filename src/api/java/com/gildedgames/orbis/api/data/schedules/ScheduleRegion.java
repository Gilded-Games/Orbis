package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.region.IColored;
import com.gildedgames.orbis.api.data.region.IMutableRegion;
import com.gildedgames.orbis.api.inventory.InventorySpawnEggs;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.gildedgames.orbis.api.world.IWorldObject;
import net.minecraft.nbt.NBTTagCompound;

public class ScheduleRegion implements NBT, IColored, ISchedule
{
	private InventorySpawnEggs spawnEggsInv;

	private String triggerId;

	private IMutableRegion bounds;

	private BlueprintData parent;

	private IWorldObject worldObjectParent;

	private ScheduleRegion()
	{

	}

	public ScheduleRegion(String uniqueName, IMutableRegion bounds)
	{
		this.triggerId = uniqueName;
		this.bounds = bounds;
		this.spawnEggsInv = new InventorySpawnEggs(null);
	}

	public InventorySpawnEggs getSpawnEggsInventory()
	{
		return this.spawnEggsInv;
	}

	public String getTriggerID()
	{
		return this.triggerId;
	}

	public void setTriggerId(String triggerId)
	{
		this.triggerId = triggerId;
	}

	@Override
	public IMutableRegion getBounds()
	{
		return this.bounds;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		tag.setString("triggerId", this.triggerId);
		funnel.set("bounds", this.bounds);
		funnel.set("spawnEggsInv", this.spawnEggsInv);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.triggerId = tag.getString("triggerId");
		this.bounds = funnel.get("bounds");
		this.spawnEggsInv = funnel.get("spawnEggsInv");
	}

	@Override
	public int getColor()
	{
		return 0xd19044;
	}

	@Override
	public IWorldObject getWorldObjectParent()
	{
		return this.worldObjectParent;
	}

	@Override
	public void setWorldObjectParent(IWorldObject parent)
	{
		this.worldObjectParent = parent;
	}

	@Override
	public BlueprintData getParent()
	{
		return this.parent;
	}

	@Override
	public void setParent(BlueprintData parent)
	{
		this.parent = parent;

		this.spawnEggsInv.setBlueprintData(this.parent);
	}
}
