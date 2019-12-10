package com.gildedgames.orbis.common.containers;

import com.gildedgames.orbis.lib.util.mc.SlotHashed;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

public class SlotGroup
{
	private Pair<SlotHashed, SlotHashed> slots;

	private int layerId;

	public SlotGroup(Pair<SlotHashed, SlotHashed> slots, int layerId)
	{
		this.slots = slots;
		this.layerId = layerId;
	}

	public Pair<SlotHashed, SlotHashed> getSlots()
	{
		return this.slots;
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.layerId);

		return builder.toHashCode();
	}
}