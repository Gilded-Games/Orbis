package com.gildedgames.orbis.client;

import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.data.BlueprintData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.client.renderers.RenderBlueprintBlocks;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.google.common.base.Optional;
import com.google.common.cache.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.concurrent.TimeUnit;

public class OrbisClientCaches
{

	@SideOnly(Side.CLIENT)
	private static final LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> blueprintRenderCache = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(new BlueprintRenderCacheRL())
			.build(
					new CacheLoader<IDataIdentifier, Optional<RenderBlueprintBlocks>>()
					{
						@Override
						public Optional<RenderBlueprintBlocks> load(final IDataIdentifier id)
						{
							try
							{
								final BlueprintData data = OrbisCore.getProjectManager().findData(id);

								final RenderBlueprintBlocks blueprint = new RenderBlueprintBlocks(
										new Blueprint(Minecraft.getMinecraft().world, BlockPos.ORIGIN, data),
										Minecraft.getMinecraft().world);

								return Optional.of(blueprint);
							}
							catch (final OrbisMissingDataException e)
							{
								OrbisCore.LOGGER.error("Missing in blueprint render cache: " + e);
							}

							return Optional.absent();
						}
					});

	@SideOnly(Side.CLIENT)
	public static LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> getBlueprintRenders()
	{
		return blueprintRenderCache;
	}

	private static class BlueprintRenderCacheRL implements RemovalListener<IDataIdentifier, Optional<RenderBlueprintBlocks>>
	{

		@Override
		public void onRemoval(final RemovalNotification<IDataIdentifier, Optional<RenderBlueprintBlocks>> notification)
		{
			final Optional<RenderBlueprintBlocks> opt = notification.getValue();

			if (opt == null)
			{
				return;
			}

			final RenderBlueprintBlocks blueprint = opt.orNull();

			if (blueprint != null)
			{
				blueprint.onRemoved();
			}
		}
	}

}
