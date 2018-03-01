package com.gildedgames.orbis.api.data.schedules;

import com.gildedgames.orbis.api.util.ObjectFilter;
import com.gildedgames.orbis.api.util.RegionHelp;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.IWorldObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;
import java.util.Map;

public class ScheduleRecord implements IScheduleRecord
{

	private final List<IScheduleRecordListener> listeners = Lists.newArrayList();

	private Map<Integer, ISchedule> schedules = Maps.newHashMap();

	private IWorldObject worldObjectParent;

	private IScheduleLayer parent;

	public ScheduleRecord()
	{

	}

	@Override
	public void listen(IScheduleRecordListener listener)
	{
		if (!this.listeners.contains(listener))
		{
			this.listeners.add(listener);
		}
	}

	@Override
	public boolean unlisten(IScheduleRecordListener listener)
	{
		return this.listeners.remove(listener);
	}

	@Override
	public void removeSchedule(int id)
	{
		ISchedule schedule = this.schedules.remove(id);

		this.listeners.forEach(o -> o.onRemoveSchedule(schedule));
	}

	@Override
	public int addSchedule(ISchedule schedule)
	{
		int id = this.schedules.size();

		this.setSchedule(id, schedule);

		return id;
	}

	@Override
	public void setSchedule(int id, ISchedule schedule)
	{
		if (schedule instanceof ScheduleRegion)
		{
			ScheduleRegion scheduleRegion = (ScheduleRegion) schedule;

			boolean intersects = false;

			for (ScheduleRegion s : this.getSchedules(ScheduleRegion.class))
			{
				if (RegionHelp.intersects(scheduleRegion.getBounds(), s.getBounds()))
				{
					intersects = true;
					break;
				}
			}

			if (intersects)
			{
				throw new IllegalArgumentException("Schedule regions cannot intersect with other schedule regions in the Blueprint");
			}
		}

		schedule.setWorldObjectParent(this.worldObjectParent);
		schedule.setParent(this);

		this.schedules.put(id, schedule);

		this.listeners.forEach(o -> o.onAddSchedule(schedule));
	}

	@Override
	public int getScheduleId(ISchedule schedule)
	{
		for (Map.Entry<Integer, ISchedule> entry : this.schedules.entrySet())
		{
			int i = entry.getKey();
			final ISchedule s = entry.getValue();

			if (schedule.equals(s))
			{
				return i;
			}
		}

		return -1;
	}

	@Override
	public <T extends ISchedule> T getSchedule(int id)
	{
		return (T) this.schedules.get(id);
	}

	@Override
	public <T extends ISchedule> List<T> getSchedules(Class<T> clazz)
	{
		return ObjectFilter.getTypesFrom(this.schedules.values(), clazz);
	}

	@Override
	public List<ISchedule> getSchedulesFromTriggerID(String triggerId)
	{
		List<ISchedule> schedules = Lists.newArrayList();

		for (ScheduleRegion s : this.getSchedules(ScheduleRegion.class))
		{
			if (s.getTriggerId().equals(triggerId))
			{
				schedules.add(s);
			}
		}

		return schedules;
	}

	@Override
	public IScheduleLayer getParent()
	{
		return this.parent;
	}

	@Override
	public void setParent(IScheduleLayer parent)
	{
		this.parent = parent;
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setIntMap("schedules", this.schedules);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.schedules = funnel.getIntMap("schedules");

		this.schedules.values().forEach(s -> s.setParent(this));
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

		this.schedules.values().forEach(s -> s.setWorldObjectParent(this.worldObjectParent));
	}
}
