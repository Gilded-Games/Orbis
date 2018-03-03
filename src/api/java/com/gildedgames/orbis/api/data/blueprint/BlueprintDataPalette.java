package com.gildedgames.orbis.api.data.blueprint;

import com.gildedgames.orbis.api.OrbisAPI;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.DataCondition;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.data.region.IDimensions;
import com.gildedgames.orbis.api.data.region.Region;
import com.gildedgames.orbis.api.util.io.NBTFunnel;
import com.gildedgames.orbis.api.util.mc.NBT;
import com.google.common.collect.Maps;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * TODO: MOVE THIS INTO API PACKAGE AND HAVE A UNIVERSAL PROJECT MANAGER. VERY IMPORTANT.
 *
 * Right now project management is only a part of the Orbis interface itself
 * (the tool), instead of the API. This means that palettes and other objects
 * from the API package cannot attempt to find state.
 */
public class BlueprintDataPalette implements NBT
{

	private final Map<IDataIdentifier, BlueprintData> data = Maps.newHashMap();

	private LinkedHashMap<IDataIdentifier, DataCondition> idToConditions = Maps.newLinkedHashMap();

	private IDimensions largestDim;

	private int minEntrances, maxEntrances;

	public BlueprintDataPalette()
	{

	}

	public Collection<IDataIdentifier> getIDs()
	{
		return this.idToConditions.keySet();
	}

	public Map<IDataIdentifier, DataCondition> getIDToConditions()
	{
		return this.idToConditions;
	}

	public BlueprintData fetchRandom(final World world, final Random rand)
	{
		final float randomValue = rand.nextFloat() * this.totalChance();
		float chanceSum = 0.0f;

		for (final Map.Entry<IDataIdentifier, DataCondition> pair : this.idToConditions.entrySet())
		{
			final DataCondition condition = pair.getValue();

			if (condition.isMet(randomValue, chanceSum, rand, world))
			{
				return this.data.get(pair.getKey());
			}

			chanceSum += condition.getWeight();
		}

		return null;
	}

	public float totalChance()
	{
		float total = 0f;

		for (final Map.Entry<IDataIdentifier, DataCondition> pair : this.idToConditions.entrySet())
		{
			final DataCondition condition = pair.getValue();

			total += condition.getWeight();
		}

		return total;
	}

	public Collection<BlueprintData> getData()
	{
		return this.data.values();
	}

	public IDimensions getLargestDim()
	{
		return this.largestDim;
	}

	public int getMinimumEntrances()
	{
		return this.minEntrances;
	}

	public int getMaximumEntrances()
	{
		return this.maxEntrances;
	}

	private void evaluateEntrances()
	{
		int max = Integer.MIN_VALUE, min = Integer.MAX_VALUE;

		for (final BlueprintData blueprint : this.data.values())
		{
			if (blueprint.entrances().size() > max)
			{
				max = blueprint.entrances().size();
			}

			if (blueprint.entrances().size() < min)
			{
				min = blueprint.entrances().size();
			}
		}

		this.minEntrances = min;
		this.maxEntrances = max;
	}

	private void evaluateLargestInArea()
	{
		int width = Integer.MIN_VALUE;
		int height = Integer.MIN_VALUE;
		int length = Integer.MIN_VALUE;

		for (final BlueprintData blueprint : this.data.values())
		{
			width = Math.max(width, blueprint.getWidth());
			height = Math.max(height, blueprint.getHeight());
			length = Math.max(length, blueprint.getLength());
		}

		this.largestDim = new Region(BlockPos.ORIGIN, new BlockPos(width - 1, height - 1, length - 1));
	}

	public void add(final BlueprintData data, final DataCondition condition)
	{
		this.idToConditions.put(data.getMetadata().getIdentifier(), condition);
		this.data.put(data.getMetadata().getIdentifier(), data);

		this.evaluateLargestInArea();
		this.evaluateEntrances();
	}

	public void remove(final BlueprintData data)
	{
		IDataIdentifier toRemove = null;

		for (final Map.Entry<IDataIdentifier, DataCondition> pair : this.idToConditions.entrySet())
		{
			if (pair.getKey().equals(data.getMetadata().getIdentifier()))
			{
				toRemove = pair.getKey();
				break;
			}
		}

		if (toRemove == null)
		{
			return;
		}

		this.data.remove(toRemove);
		this.idToConditions.remove(toRemove);

		this.evaluateLargestInArea();
	}

	@Override
	public void write(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		funnel.setMap("idToConditions", this.idToConditions);
	}

	@Override
	public void read(final NBTTagCompound tag)
	{
		final NBTFunnel funnel = new NBTFunnel(tag);

		this.idToConditions = Maps.newLinkedHashMap(funnel.getMap("idToConditions"));

		for (final Map.Entry<IDataIdentifier, DataCondition> pair : this.idToConditions.entrySet())
		{
			try
			{
				final IDataIdentifier id = pair.getKey();
				final BlueprintData data = OrbisAPI.services().getProjectManager().findData(id);

				this.data.put(id, data);
			}
			catch (final OrbisMissingDataException | OrbisMissingProjectException e)
			{
				OrbisAPI.LOGGER.error("Missing in " + this.getClass().getName() + " : ", e);
			}
		}

		this.evaluateLargestInArea();
		this.evaluateEntrances();
	}

	@Override
	public int hashCode()
	{
		final HashCodeBuilder builder = new HashCodeBuilder();

		builder.append(this.idToConditions);

		return builder.toHashCode();
	}
}
