package com.gildedgames.orbis.common.player.godmode.selection_input;

import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.client.godmode.selection_inputs.ISelectionInputClient;
import com.gildedgames.orbis.client.godmode.selection_inputs.SelectionInputDraggedClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketActiveSelection;
import com.gildedgames.orbis.common.player.godmode.IGodPower;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import com.gildedgames.orbis_api.OrbisAPI;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.shapes.AbstractShape;
import com.gildedgames.orbis_api.world.IWorldObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.MouseEvent;

public class SelectionInputDragged implements ISelectionInput
{
	private final EntityPlayer player;

	private ISelectionInputClient client;

	private BlockPos selectPos, prevPos;

	private IWorldObject activeSelection;

	private boolean createFromCenter, uniform;

	public SelectionInputDragged(final EntityPlayer player, final String displayName, final ResourceLocation texture)
	{
		this.player = player;

		if (player.getEntityWorld().isRemote)
		{
			this.client = new SelectionInputDraggedClient(displayName, texture);
		}
	}

	public BlockPos getPrevPos()
	{
		return this.prevPos;
	}

	public BlockPos getSelectPos()
	{
		return this.selectPos;
	}

	public SelectionInputDragged createFromCenter(final boolean flag)
	{
		this.createFromCenter = flag;

		return this;
	}

	public SelectionInputDragged uniform(final boolean flag)
	{
		this.uniform = flag;

		return this;
	}

	private void processSelectionMode(final AbstractShape shape)
	{
		shape.setCreateFromCenter(this.createFromCenter);
		shape.setUniform(this.uniform);
	}

	private boolean setActiveSelectionCorner(final BlockPos pos)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(this.player);
		final IGodPower power = playerOrbis.powers().getCurrentPower();

		IShapeSelector selector = power.getShapeSelector();

		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		if (held.getItem() instanceof IShapeSelector)
		{
			selector = (IShapeSelector) held.getItem();
		}

		if (this.activeSelection != null)
		{
			if (this.activeSelection.getShape() instanceof AbstractShape)
			{
				this.processSelectionMode((AbstractShape) this.activeSelection.getShape());
			}

			if (selector.canSelectShape(playerOrbis, this.activeSelection.getShape(), playerOrbis.getWorld()))
			{
				if (playerOrbis.getWorld().isRemote)
				{
					final BlockPos endPos = RaytraceHelp.doOrbisRaytrace(playerOrbis);

					OrbisCore.network().sendPacketToServer(new PacketActiveSelection(this.activeSelection.getShape(), this.selectPos, endPos));
				}

				this.activeSelection = null;
				this.selectPos = null;
				this.prevPos = null;

				return true;
			}
		}
		else if (selector.isSelectorActive(playerOrbis, playerOrbis.getWorld()) && selector.canStartSelectingFrom(playerOrbis, pos))
		{
			this.selectPos = pos;

			final IShape newSelection = playerOrbis.selectionTypes().getCurrentSelectionType().createShape(pos, pos, playerOrbis, this.createFromCenter);

			this.activeSelection = new WorldShape(newSelection, this.player.getEntityWorld());
		}

		return false;
	}

	@Override
	public boolean shouldClearSelectionOnEscape()
	{
		return true;
	}

	@Override
	public void onUpdate(final boolean isActive, final IShapeSelector selector)
	{
		final PlayerOrbis playerOrbis = PlayerOrbis.get(this.player);

		if (isActive)
		{
			if (this.selectPos != null)
			{
				final BlockPos endPos = RaytraceHelp.doOrbisRaytrace(playerOrbis);

				if (this.activeSelection != null && !endPos.equals(this.prevPos))
				{
					this.prevPos = endPos;
					final IShape newSelection = playerOrbis.selectionTypes().getCurrentSelectionType()
							.createShape(this.selectPos, endPos, playerOrbis, this.createFromCenter);

					this.activeSelection = new WorldShape(newSelection, this.player.getEntityWorld());
				}
			}

			if (!playerOrbis.inDeveloperMode() || !selector.isSelectorActive(playerOrbis, playerOrbis.getWorld()))
			{
				this.activeSelection = null;
			}

			if (this.activeSelection != null && this.activeSelection.getShape() instanceof AbstractShape)
			{
				this.processSelectionMode((AbstractShape) this.activeSelection.getShape());
			}
		}
		else
		{
			this.activeSelection = null;
		}
	}

	@Override
	public void onMouseEvent(final MouseEvent event, final IShapeSelector selector, final PlayerOrbis playerOrbis)
	{
		if (event.getButton() == 0 || event.getButton() == 1)
		{
			if (playerOrbis.inDeveloperMode() && selector.isSelectorActive(playerOrbis, playerOrbis.getWorld()))
			{
				event.setCanceled(true);

				IGodPowerClient client = playerOrbis.powers().getCurrentPower().getClientHandler();

				Object raytracedObject = client.raytraceObject(playerOrbis);

				if (raytracedObject == null || client.onRightClickShape(playerOrbis, raytracedObject, event))
				{
					if (!event.isButtonstate() && this.getActiveSelection() != null || event.isButtonstate() && this.getActiveSelection() == null)
					{
						final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());
						this.setActiveSelectionCorner(pos);
					}
				}
			}
		}
	}

	@Override
	public void clearSelection()
	{
		this.activeSelection = null;
		this.selectPos = null;
		this.prevPos = null;
	}

	@Override
	public IWorldObject getActiveSelection()
	{
		return this.activeSelection;
	}

	@Override
	public void setActiveSelection(final IWorldObject activeSelection)
	{
		this.activeSelection = activeSelection;

		if (this.activeSelection instanceof AbstractShape)
		{
			this.processSelectionMode((AbstractShape) this.activeSelection);
		}

		if (this.player.getEntityWorld().isRemote && this.activeSelection != null)
		{
			final BlockPos endPos = RaytraceHelp.doOrbisRaytrace(PlayerOrbis.get(this.player));

			OrbisCore.network().sendPacketToServer(new PacketActiveSelection(this.activeSelection.getShape(), this.selectPos, endPos));
		}
	}

	@Override
	public ISelectionInputClient getClient()
	{
		return this.client;
	}

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}
}
