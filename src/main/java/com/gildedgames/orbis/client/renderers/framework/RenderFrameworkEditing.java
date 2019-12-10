package com.gildedgames.orbis.client.renderers.framework;

import com.gildedgames.orbis.client.renderers.RenderShape;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.framework.FrameworkNode;
import com.gildedgames.orbis.lib.data.framework.IFrameworkDataListener;
import com.gildedgames.orbis.lib.data.framework.interfaces.IFrameworkNode;
import com.gildedgames.orbis.lib.data.pathway.PathwayData;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.world.IWorldRenderer;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RenderFrameworkEditing implements IWorldRenderer, IFrameworkDataListener
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Framework framework;

	private boolean disabled = false;

	private RenderShape renderShape;

	public RenderFrameworkEditing(final Framework framework)
	{
		this.framework = framework;

		this.framework.getData().listen(this);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			this.renderShape = new RenderShape(this.framework);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = this.framework.getColor();
			this.renderShape.colorBorder = this.framework.getColor();

			this.subRenderers.add(this.renderShape);

			this.framework.getData().getNodeToPosMap().keySet().forEach(p -> this.onAddNode(p.getValue()));
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public boolean isDisabled()
	{
		return this.disabled;
	}

	@Override
	public void setDisabled(final boolean disabled)
	{
		this.disabled = disabled;
	}

	@Nullable
	@Override
	public Object getRenderedObject()
	{
		return this.framework;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.framework;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.getSelectedRegion() == this.framework && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getFrameworkPower())
		{
			boolean refresh = this.renderShape.boxAlpha == 0.25F;

			this.renderShape.boxAlpha = 0.5F;

			if (refresh)
			{
				this.renderShape.refresh();
			}
		}
		else
		{
			boolean refresh = this.renderShape.boxAlpha == 0.5F;

			this.renderShape.boxAlpha = 0.25F;

			if (refresh)
			{
				this.renderShape.refresh();
			}
		}
	}

	@Override
	public void preRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderSub(IWorldRenderer sub, World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void preRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public void postRenderAllSubs(World world, float partialTicks, boolean useCamera)
	{

	}

	@Override
	public List<IWorldRenderer> getSubRenderers(final World world)
	{
		return this.subRenderers;
	}

	@Override
	public ReadWriteLock getSubRenderersLock()
	{
		return this.lock;
	}

	@Override
	public void onRemoved()
	{
		this.framework.getData().unlisten(this);
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	@Override
	public void onAddNode(IFrameworkNode node)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			RenderFrameworkNode renderNode = new RenderFrameworkNode(this.framework, node);

			this.subRenderers.add(renderNode);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onRemoveNode(IFrameworkNode node)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer toRemove = null;

			for (IWorldRenderer renderer : this.subRenderers)
			{
				if (renderer.getRenderedObject() == node)
				{
					toRemove = renderer;
					break;
				}
			}

			this.subRenderers.remove(toRemove);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onAddEdge(FrameworkNode n1, FrameworkNode n2)
	{

	}

	@Override
	public void onAddIntersection(PathwayData pathway1, PathwayData pathway2, BlueprintData blueprint)
	{

	}
}
