package com.gildedgames.orbis.api.core;

import com.gildedgames.orbis.api.core.util.BlueprintPlacer;
import com.gildedgames.orbis.api.processing.DataPrimer;
import com.gildedgames.orbis.api.processing.IBlockAccessExtended;
import com.gildedgames.orbis.api.world.IWorldGen;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlueprintWorldGen implements IWorldGen
{

	private final BlueprintDefinition def;

	private final BlueprintDefinitionPool defPool;

	public BlueprintWorldGen(final BlueprintDefinitionPool defPool)
	{
		this.def = null;
		this.defPool = defPool;
	}

	public BlueprintWorldGen(final BlueprintDefinition def)
	{
		this.def = def;
		this.defPool = null;
	}

	@Override
	public boolean generate(final IBlockAccessExtended blockAccess, final World world, final Random rand, final BlockPos position, final boolean centered)
	{
		final Rotation rotation = (this.def != null ? this.def.hasRandomRotation() : this.defPool.hasRandomRotation()) ?
				BlueprintPlacer.ROTATIONS[rand.nextInt(BlueprintPlacer.ROTATIONS.length)] :
				BlueprintPlacer.ROTATIONS[0];

		final ICreationData data = new CreationData(world).pos(position);

		data.rotation(rotation);

		return BlueprintPlacer
				.place(new DataPrimer(blockAccess), this.def == null ? this.defPool.getRandomDefinition(rand) : this.def,
						data, rand);
	}

	@Override
	public boolean generate(final IBlockAccessExtended blockAccess, final World world, final Random rand, final BlockPos position)
	{
		return this.generate(blockAccess, world, rand, position, false);
	}
}