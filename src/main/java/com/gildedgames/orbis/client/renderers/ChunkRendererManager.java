package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.common.OrbisCapabilities;
import com.gildedgames.orbis.common.capabilities.chunk_renderer.IChunkRendererCapability;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbisObserver;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.region.Region;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.gildedgames.orbis_api.world.IWorldObjectManagerObserver;
import com.gildedgames.orbis_api.world.IWorldRenderer;
import com.gildedgames.orbis_api.world.WorldObjectManager;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChunkRendererManager implements PlayerOrbisObserver, IWorldObjectManagerObserver
{

	private final static Minecraft mc = Minecraft.getMinecraft();

	private final List<IWorldRenderer> allRenders = Lists.newArrayList();

	/**
	 * The active renderer for the player, for example a blueprint in its hand or a region he's selecting.
	 */
	private final List<IWorldRenderer> playerRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	public static IChunkRendererCapability getChunkRenderer(final World world, final int chunkX, final int chunkZ)
	{
		if (world == null)
		{
			throw new IllegalArgumentException("The world passed into getChunkRenderer is null");
		}

		return world.getChunkFromChunkCoords(chunkX, chunkZ).getCapability(OrbisCapabilities.CHUNK_RENDERER, EnumFacing.UP);
	}

	public void unload()
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.allRenders.clear();
		}
		finally
		{
			w.unlock();
		}

		this.playerRenderers.clear();
	}

	public void render(final World world, final float partialTicks)
	{
		final int renderDistance = mc.gameSettings.renderDistanceChunks;

		final BlockPos playerPos = mc.player.getPosition();

		final int blockDist = renderDistance << 4;
		final BlockPos minPos = new BlockPos(playerPos.getX() - blockDist, 0, playerPos.getZ() - blockDist);
		final BlockPos maxPos = new BlockPos(playerPos.getX() + blockDist, 256, playerPos.getZ() + blockDist);

		final IRegion encompassing = new Region(minPos, maxPos);

		GlStateManager.pushMatrix();

		final Lock w = this.lock.readLock();
		w.lock();

		try
		{
			for (final Iterator<IWorldRenderer> it = this.allRenders.iterator(); it.hasNext(); )
			{
				final IWorldRenderer renderer = it.next();

				this.render(world, renderer, partialTicks, encompassing);
			}
		}
		finally
		{
			w.unlock();
		}

		final int minChunkX = minPos.getX() >> 4;
		final int minChunkZ = minPos.getZ() >> 4;

		for (int x = minChunkX; x < minChunkX + 2 * renderDistance; x++)
		{
			for (int z = minChunkZ; z < minChunkZ + 2 * renderDistance; z++)
			{
				this.load(world, x, z);

				final IChunkRendererCapability chunk = getChunkRenderer(world, x, z);

				if (chunk != null)
				{
					chunk.render(world, partialTicks);
				}
			}
		}

		GlStateManager.popMatrix();
	}

	public void render(World world, IWorldRenderer renderer, float partialTicks)
	{
		this.render(world, renderer, partialTicks, null);
	}

	public void render(final World world, final IWorldRenderer renderer, final float partialTicks, final IRegion encompassing)
	{
		if (!renderer.isDisabled())
		{
			final Lock w = renderer.getSubRenderersLock().readLock();
			w.lock();

			try
			{
				GlStateManager.pushMatrix();
				renderer.preRenderAllSubs(world, partialTicks, true);

				for (final IWorldRenderer sub : renderer.getSubRenderers(world))
				{
					renderer.preRenderSub(sub, world, partialTicks, true);

					this.render(world, sub, partialTicks, encompassing);

					renderer.postRenderSub(sub, world, partialTicks, true);
				}

				renderer.postRenderAllSubs(world, partialTicks, true);
				GlStateManager.popMatrix();
			}
			finally
			{
				w.unlock();
			}

			if (encompassing == null || RegionHelp.intersects2D(renderer.getBoundingBox(), encompassing))
			{
				renderer.render(world, partialTicks, true);
			}
		}
	}

	public void load(final World world, final int chunkX, final int chunkZ)
	{
		final IChunkRendererCapability chunk = getChunkRenderer(world, chunkX, chunkZ);

		if (chunk != null && !chunk.hasBeenLoaded())
		{
			final Lock w = this.lock.readLock();
			w.lock();

			try
			{
				for (final Iterator<IWorldRenderer> it = this.allRenders.iterator(); it.hasNext(); )
				{
					final IWorldRenderer renderer = it.next();

					if (chunk.shouldHave(renderer))
					{
						chunk.addRenderer(renderer);
					}
				}

				chunk.load();
			}
			finally
			{
				w.unlock();
			}
		}
	}

	public void addRenderer(final World world, final IWorldRenderer renderer)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.allRenders.add(renderer);
		}
		finally
		{
			w.unlock();
		}

		final IRegion region = renderer.getBoundingBox();

		final BlockPos min = region.getMin();
		final BlockPos max = region.getMax();

		final int minChunkX = min.getX() >> 4;
		final int minChunkZ = min.getZ() >> 4;

		final int maxChunkX = max.getX() >> 4;
		final int maxChunkZ = max.getZ() >> 4;

		for (int x = minChunkX; x <= maxChunkX; x++)
		{
			for (int z = minChunkZ; z <= maxChunkZ; z++)
			{
				final IChunkRendererCapability chunk = getChunkRenderer(world, x, z);

				if (chunk != null)
				{
					chunk.addRenderer(renderer);
				}
			}
		}
	}

	public void removeRenderer(final World world, final IWorldRenderer renderer)
	{
		renderer.onRemoved();

		for (final IWorldRenderer sub : renderer.getSubRenderers(mc.world))
		{
			sub.onRemoved();
		}

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.allRenders.remove(renderer);
		}
		finally
		{
			w.unlock();
		}

		final BlockPos min = renderer.getBoundingBox().getMin();
		final BlockPos max = renderer.getBoundingBox().getMax();

		final int minChunkX = min.getX() >> 4;
		final int minChunkZ = min.getZ() >> 4;

		final int maxChunkX = max.getX() >> 4;
		final int maxChunkZ = max.getZ() >> 4;

		for (int x = minChunkX; x <= maxChunkX; x++)
		{
			for (int z = minChunkZ; z <= maxChunkZ; z++)
			{
				final IChunkRendererCapability chunk = getChunkRenderer(world, x, z);

				if (chunk != null)
				{
					chunk.removeRenderer(renderer);
				}
			}
		}
	}

	public void updateRenderer(final World world, final IWorldRenderer renderer)
	{
		this.removeRenderer(world, renderer);
		this.addRenderer(world, renderer);
	}

	@Override
	public void onUpdate(final PlayerOrbis playerOrbis)
	{
		final List<IWorldRenderer> activeRenderers = playerOrbis.getActiveRenderers();
		final List<IWorldRenderer> renderersToRemove = Lists.newArrayList();

		for (final IWorldRenderer playerRenderer : this.playerRenderers)
		{
			if (!activeRenderers.contains(playerRenderer))
			{
				renderersToRemove.add(playerRenderer);
			}
		}

		for (final IWorldRenderer renderer : renderersToRemove)
		{
			this.removeRenderer(playerOrbis.getEntity().world, renderer);
			this.playerRenderers.remove(renderer);
		}

		if (activeRenderers != null && !activeRenderers.isEmpty())
		{
			for (final IWorldRenderer renderer : activeRenderers)
			{
				if (!this.playerRenderers.contains(renderer))
				{
					this.addPlayerRenderer(playerOrbis.getEntity().world, renderer);
				}
			}
		}
	}

	private void addPlayerRenderer(final World world, final IWorldRenderer renderer)
	{
		this.playerRenderers.add(renderer);
		this.addRenderer(world, renderer);
	}

	@Override
	public void onObjectAdded(final WorldObjectManager manager, final IWorldObject object)
	{
		this.addRenderer(object.getWorld(), object.getRenderer());
	}

	@Override
	public void onObjectRemoved(final WorldObjectManager manager, final IWorldObject object)
	{
		if (object instanceof IShape)
		{
			final Lock w = this.lock.writeLock();
			w.lock();

			try
			{
				this.allRenders.removeIf(renderer -> object == renderer.getRenderedObject());
			}
			finally
			{
				w.unlock();
			}
		}
	}

	@Override
	public void onReloaded(final WorldObjectManager manager)
	{
		for (final IWorldObject object : manager.getObjects())
		{
			object.setWorld(manager.getWorld());

			this.onObjectAdded(manager, object);
		}
	}

}
