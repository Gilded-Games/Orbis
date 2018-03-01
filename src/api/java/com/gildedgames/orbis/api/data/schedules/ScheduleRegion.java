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

	private IWorldObject worldObjectParent;

	private IScheduleRecord parent;

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

	@Override
	public String getTriggerId()
	{
		return this.triggerId;
	}

	@Override
	public void setTriggerId(String triggerId)
	{
		this.triggerId = triggerId;
	}

	@Override
	public IScheduleRecord getParent()
	{
		return this.parent;
	}

	@Override
	public void setParent(IScheduleRecord parent)
	{
		this.parent = parent;
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

		if (parent.getData() instanceof BlueprintData)
		{
			BlueprintData data = (BlueprintData) parent.getData();

			this.spawnEggsInv.setBlueprintData(data);
		}
	}
}
