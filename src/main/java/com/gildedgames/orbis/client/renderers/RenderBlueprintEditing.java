package com.gildedgames.orbis.client.renderers;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_objects.Blueprint;
import com.gildedgames.orbis.lib.core.tree.INode;
import com.gildedgames.orbis.lib.core.tree.INodeTreeListener;
import com.gildedgames.orbis.lib.core.tree.LayerLink;
import com.gildedgames.orbis.lib.data.blueprint.IBlueprintDataListener;
import com.gildedgames.orbis.lib.data.pathway.IEntrance;
import com.gildedgames.orbis.lib.data.region.IRegion;
import com.gildedgames.orbis.lib.data.schedules.IScheduleLayer;
import com.gildedgames.orbis.lib.data.schedules.IScheduleLayerHolderListener;
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

public class RenderBlueprintEditing
		implements IWorldRenderer, IScheduleLayerHolderListener, IBlueprintDataListener, INodeTreeListener<IScheduleLayer, LayerLink>
{
	private final List<IWorldRenderer> subRenderers = Lists.newArrayList();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final Blueprint blueprint;

	private boolean disabled = false;

	private INode<IScheduleLayer, LayerLink> focusedLayer;

	private RenderScheduleLayer focusedRender;

	private RenderShape renderShape;

	public RenderBlueprintEditing(final Blueprint blueprint)
	{
		this.blueprint = blueprint;

		this.blueprint.listen(this);
		this.blueprint.getData().listen(this);
		this.blueprint.getData().getScheduleLayerTree().listen(this);

		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			for (Integer id : this.blueprint.getData().getScheduleLayerTree().getInternalMap().keySet())
			{
				INode<IScheduleLayer, LayerLink> layer = this.blueprint.getData().getScheduleLayerTree().get(id);

				if (layer != null)
				{
					this.subRenderers.add(new RenderScheduleLayer(layer, this.blueprint, this.blueprint, false));
				}
			}

			if (this.blueprint.getData().getEntrance() != null)
			{
				this.onAddEntrance(this.blueprint.getData().getEntrance());
			}

			this.renderShape = new RenderShape(this.blueprint);

			this.renderShape.useCustomColors = true;

			this.renderShape.colorGrid = this.blueprint.getColor();
			this.renderShape.colorBorder = this.blueprint.getColor();

			this.subRenderers.add(this.renderShape);
		}
		finally
		{
			w.unlock();
		}

		this.onChangeScheduleLayerNode(null, -1, this.blueprint.getCurrentScheduleLayerNode(), this.blueprint.getCurrentScheduleLayerIndex());
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
		return this.blueprint;
	}

	@Override
	public IRegion getBoundingBox()
	{
		return this.blueprint;
	}

	@Override
	public void render(final World world, final float partialTicks, boolean useCamera)
	{
		PlayerOrbis playerOrbis = PlayerOrbis.get(Minecraft.getMinecraft().player);

		if (playerOrbis.getSelectedRegion() == this.blueprint && playerOrbis.powers().getCurrentPower() == playerOrbis.powers().getBlueprintPower())
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
		this.blueprint.unlisten(this);
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
	public void onChangeScheduleLayerNode(INode<IScheduleLayer, LayerLink> prevLayer, int prevIndex, INode<IScheduleLayer, LayerLink> newLayer, int newIndex)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			final INode<IScheduleLayer, LayerLink> layer = this.blueprint.getData().getScheduleLayerTree().get(newIndex);

			if (layer != null)
			{
				RenderScheduleLayer found = null;

				for (IWorldRenderer r : this.subRenderers)
				{
					if (r instanceof RenderScheduleLayer)
					{
						RenderScheduleLayer s = (RenderScheduleLayer) r;

						if (s.getRenderedObject() == layer)
						{
							found = s;
							break;
						}
					}
				}

				if (this.focusedLayer != null && this.focusedRender != null)
				{
					this.focusedRender.setFocused(false);
				}

				this.focusedLayer = layer;

				if (found == null)
				{
					this.focusedRender = new RenderScheduleLayer(this.focusedLayer, this.blueprint, this.blueprint, false);
					this.focusedRender.setFocused(true);

					this.subRenderers.add(this.focusedRender);
				}
				else
				{
					this.focusedRender = found;
					this.focusedRender.setFocused(true);
				}
			}
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onSetData(INode<IScheduleLayer, LayerLink> node, IScheduleLayer iScheduleLayer, int id)
	{

	}

	@Override
	public void onPut(INode<IScheduleLayer, LayerLink> node, int id)
	{

	}

	@Override
	public void onRemove(INode<IScheduleLayer, LayerLink> layer, int index)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer toRemove = null;

			for (IWorldRenderer renderer : this.subRenderers)
			{
				if (renderer.getRenderedObject() == layer)
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
	public void onDataChanged()
	{

	}

	@Override
	public void onAddEntrance(IEntrance entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			RenderEntrance render = new RenderEntrance(this.blueprint, entrance);

			this.subRenderers.add(render);
		}
		finally
		{
			w.unlock();
		}
	}

	@Override
	public void onRemoveEntrance(IEntrance entrance)
	{
		final Lock w = this.lock.writeLock();
		w.lock();

		try
		{
			IWorldRenderer toRemove = null;

			for (IWorldRenderer renderer : this.subRenderers)
			{
				if (renderer.getRenderedObject() == entrance)
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
}
