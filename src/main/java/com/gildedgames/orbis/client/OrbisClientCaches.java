package com.gildedgames.orbis.client;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.blueprint.BlueprintStackerData;
import com.gildedgames.orbis.api.data.framework.FrameworkData;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.world.IWorldRenderer;
import com.gildedgames.orbis.client.renderers.RenderBlueprintBlocks;
import com.gildedgames.orbis.client.renderers.framework.RenderFrameworkEditing;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.google.common.base.Optional;
import com.google.common.cache.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OrbisClientCaches
{

	@SideOnly(Side.CLIENT)
	private static final LoadingCache<IDataIdentifier, Optional<RenderFrameworkEditing>> FRAMEWORK_RENDER_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(new WorldRendererCacheRL())
			.build(
					new CacheLoader<IDataIdentifier, Optional<RenderFrameworkEditing>>()
					{
						@Override
						public Optional<RenderFrameworkEditing> load(final IDataIdentifier id)
						{
							try
							{
								final FrameworkData data = OrbisCore.getProjectManager().findData(id);

								final RenderFrameworkEditing framework = new RenderFrameworkEditing(
										new Framework(Minecraft.getMinecraft().world, data));

								return Optional.of(framework);
							}
							catch (final OrbisMissingDataException e)
							{
								OrbisCore.LOGGER.error("Missing in OrbisClientCaches.FRAMEWORK_RENDER_CACHE: ", e);
							}

							return Optional.absent();
						}
					});

	@SideOnly(Side.CLIENT)
	private static final LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> BLUEPRINT_RENDER_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.removalListener(new WorldRendererCacheRL())
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
								OrbisCore.LOGGER.error("Missing in OrbisClientCaches.BLUEPRINT_RENDER_CACHE: ", e);
							}

							return Optional.absent();
						}
					});

	@SideOnly(Side.CLIENT)
	private static final LoadingCache<IDataIdentifier, Optional<BlockDataContainer[]>> BLUEPRINT_STACKER_BDC_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build(
					new CacheLoader<IDataIdentifier, Optional<BlockDataContainer[]>>()
					{
						@Override
						public Optional<BlockDataContainer[]> load(final IDataIdentifier id)
						{
							try
							{
								final BlueprintStackerData data = OrbisCore.getProjectManager().findData(id);

								BlockDataContainer[] bdc = new BlockDataContainer[data.getSegments().length];

								Random rand = new Random();

								for (int i = 0; i < data.getSegments().length; i++)
								{
									BlockDataContainer container = data.get(Minecraft.getMinecraft().world, rand, i);

									bdc[i] = container;
								}

								return Optional.of(bdc);
							}
							catch (final OrbisMissingDataException e)
							{
								OrbisCore.LOGGER.error("Missing in OrbisClientCaches.BLUEPRINT_STACKER_BDC_CACHE: ", e);
							}

							return Optional.absent();
						}
					});

	@SideOnly(Side.CLIENT)
	private static final LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> BLUEPRINT_STACKER_RENDER_CACHE = CacheBuilder.newBuilder()
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build(
					new CacheLoader<IDataIdentifier, Optional<RenderBlueprintBlocks>>()
					{
						@Override
						public Optional<RenderBlueprintBlocks> load(final IDataIdentifier id)
						{
							try
							{
								final BlueprintStackerData data = OrbisCore.getProjectManager().findData(id);

								BlockDataContainer container = data.get(Minecraft.getMinecraft().world, new Random(), data.getSegments().length);

								final RenderBlueprintBlocks blueprint = new RenderBlueprintBlocks(
										new Blueprint(Minecraft.getMinecraft().world, BlockPos.ORIGIN, new BlueprintData(container)),
										Minecraft.getMinecraft().world);

								return Optional.of(blueprint);
							}
							catch (final OrbisMissingDataException e)
							{
								OrbisCore.LOGGER.error("Missing in OrbisClientCaches.BLUEPRINT_STACKER_RENDER_CACHE: ", e);
							}

							return Optional.absent();
						}
					});

	@SideOnly(Side.CLIENT)
	public static LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> getBlueprintRenders()
	{
		return BLUEPRINT_RENDER_CACHE;
	}

	@SideOnly(Side.CLIENT)
	public static LoadingCache<IDataIdentifier, Optional<BlockDataContainer[]>> getBlueprintStackerBDC()
	{
		return BLUEPRINT_STACKER_BDC_CACHE;
	}

	@SideOnly(Side.CLIENT)
	public static LoadingCache<IDataIdentifier, Optional<RenderBlueprintBlocks>> getBlueprintStackerRenders()
	{
		return BLUEPRINT_STACKER_RENDER_CACHE;
	}

	@SideOnly(Side.CLIENT)
	public static LoadingCache<IDataIdentifier, Optional<RenderFrameworkEditing>> getFrameworkRenders()
	{
		return FRAMEWORK_RENDER_CACHE;
	}

	private static class WorldRendererCacheRL implements RemovalListener<IDataIdentifier, Optional<? extends IWorldRenderer>>
	{

		@Override
		public void onRemoval(final RemovalNotification<IDataIdentifier, Optional<? extends IWorldRenderer>> notification)
		{
			final Optional<? extends IWorldRenderer> opt = notification.getValue();

			if (opt == null)
			{
				return;
			}

			final IWorldRenderer worldRenderer = opt.orNull();

			if (worldRenderer != null)
			{
				worldRenderer.onRemoved();
			}
		}
	}

}
