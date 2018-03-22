package com.gildedgames.orbis.common.world_actions.impl;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.data.schedules.ISchedule;
import com.gildedgames.orbis.api.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketAddSchedule;
import com.gildedgames.orbis.common.network.packets.blueprints.PacketRemoveSchedule;
import com.gildedgames.orbis.common.world_actions.IWorldAction;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldActionAddSchedule implements IWorldAction
{

	private Blueprint blueprint;

	private ISchedule schedule;

	private int layer, blueprintId;

	private WorldActionAddSchedule()
	{

	}

	public WorldActionAddSchedule(Blueprint blueprint, ISchedule schedule, int layer)
	{
		this.blueprint = blueprint;

		this.blueprintId = WorldObjectManager.get(blueprint.getWorld()).getID(this.blueprint);

		this.schedule = schedule;
		this.layer = layer;
	}

	@Override
	public void redo(PlayerOrbis player, World world)
	{
		if (this.blueprint == null)
		{
			this.blueprint = WorldObjectManager.get(world).getObject(this.blueprintId);
		}

		IScheduleLayer layer = this.blueprint.getData().getScheduleLayer(this.layer);

		if (layer != null)
		{
			if (world.getMinecraftServer().isDedicatedServer())
			{
				layer.getScheduleRecord().addSchedule(this.schedule);
			}

			OrbisAPI.network()
					.sendPacketToDimension(new PacketAddSchedule(this.blueprint, this.schedule, this.layer), world.provider.getDimension());
		}
	}

	@Override
	public void undo(PlayerOrbis player, World world)
	{
		if (this.blueprint == null)
		{
			this.blueprint = WorldObjectManager.get(world).getObject(this.blueprintId);
		}

		IScheduleLayer layer = this.blueprint.getData().getScheduleLayer(this.layer);

		if (layer != null)
		{
			int scheduleId = layer.getScheduleRecord().getScheduleId(this.schedule);

			if (scheduleId != -1)
			{
				if (world.getMinecraftServer().isDedicatedServer())
				{
					layer.getScheduleRecord().removeSchedule(scheduleId);
				}

				OrbisAPI.network()
						.sendPacketToDimension(new PacketRemoveSchedule(this.blueprint, this.schedule), world.provider.getDimension());
			}
		}
	}

	@Override
	public void setWorld(PlayerOrbis playerOrbis, World world)
	{
		this.blueprint.setWorld(world);
	}

	@Override
	public void write(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		tag.setInteger("b", this.blueprintId);
		funnel.set("s", this.schedule);
		tag.setInteger("l", this.layer);
	}

	@Override
	public void read(NBTTagCompound tag)
	{
		NBTFunnel funnel = new NBTFunnel(tag);

		this.blueprintId = tag.getInteger("b");
		this.schedule = funnel.get("s");
		this.layer = tag.getInteger("l");
	}
}
