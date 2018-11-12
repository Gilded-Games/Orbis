package com.gildedgames.orbis.common.player.godmode.selection_input;

import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.client.godmode.selection_inputs.ISelectionInputClient;
import com.gildedgames.orbis.client.godmode.selection_inputs.SelectionInputBrushClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.network.packets.PacketActiveSelectionMultiple;
import com.gildedgames.orbis.common.player.godmode.IGodPower;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import com.gildedgames.orbis_api.data.region.IRegion;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.data.shapes.AbstractShape;
import com.gildedgames.orbis_api.util.RotationHelp;
import com.gildedgames.orbis_api.world.IWorldObject;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import org.lwjgl.input.Mouse;

import java.util.Set;

public class SelectionInputBrush implements ISelectionInput
{
	private final PlayerOrbis playerOrbis;

	private ISelectionInputClient client;

	private IWorldObject activeSelection;

	private BlockPos changingSizeOrigin, changingSizePos;

	private boolean changed, changingSize;

	private IGodPower prevPower;

	private Set<BlockPos> paintedPositions = Sets.newHashSet();

	private boolean painting;

	public SelectionInputBrush(final PlayerOrbis playerOrbis, final World world)
	{
		this.playerOrbis = playerOrbis;

		if (world.isRemote)
		{
			this.client = new SelectionInputBrushClient();
		}
	}

	@Override
	public boolean shouldClearSelectionOnEscape()
	{
		return false;
	}

	@Override
	public void onUpdate(final boolean isActive, final IShapeSelector selector)
	{
		final World world = this.playerOrbis.getWorld();

		if (isActive && selector.isSelectorActive(this.playerOrbis, this.playerOrbis.getWorld()))
		{
			if (this.prevPower != this.playerOrbis.powers().getCurrentPower())
			{
				this.prevPower = this.playerOrbis.powers().getCurrentPower();

				this.changed = true;
			}

			final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(this.playerOrbis.getEntity());

			if (!pos.equals(this.changingSizePos) && this.changingSize)
			{
				this.changingSizePos = pos;

				this.changed = true;
			}

			if (this.activeSelection == null || this.changed)
			{
				final IShape newSelection = this.playerOrbis.selectionTypes().getCurrentSelectionType()
						.createShape(this.changingSizeOrigin != null ? this.changingSizeOrigin : pos, this.changingSizePos != null ? this.changingSizePos : pos,
								this.playerOrbis,
								true);

				this.activeSelection = new WorldShape(newSelection, this.playerOrbis.getWorld());

				this.changed = false;
			}

			if (!this.changingSize)
			{
				final IRegion centered = RotationHelp.regionFromCenter(pos, this.activeSelection.getShape().getBoundingBox(), Rotation.NONE);

				this.activeSelection.setPos(centered.getMin().add(0, -centered.getHeight() / 2, 0));
			}

			if (this.activeSelection.getShape() instanceof AbstractShape)
			{
				final AbstractShape shape = (AbstractShape) this.activeSelection.getShape();

				shape.setCreateFromCenter(true);
				shape.setUniform(true);
			}

			if (world.isRemote)
			{
				if (Mouse.isButtonDown(0) && this.painting && Minecraft.getMinecraft().currentScreen == null)
				{
					if (!this.paintedPositions.contains(pos))
					{
						this.paintedPositions.add(pos.add(-this.activeSelection.getShape().getBoundingBox().getWidth() / 2,
								-this.activeSelection.getShape().getBoundingBox().getHeight() / 2,
								-this.activeSelection.getShape().getBoundingBox().getLength() / 2));
					}
				}
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
		if (playerOrbis.inDeveloperMode() && selector.isSelectorActive(playerOrbis, playerOrbis.getWorld()))
		{
			if (event.getButton() == 1 || event.getButton() == 0)
			{
				event.setCanceled(true);

				if (event.getButton() == 0)
				{
					this.painting = event.isButtonstate();

					if (!this.painting)
					{
						if (this.playerOrbis.getWorld().isRemote && this.activeSelection != null)
						{
							OrbisCore.network()
									.sendPacketToServer(new PacketActiveSelectionMultiple(this.activeSelection.getShape(), this.paintedPositions));
						}

						this.paintedPositions.clear();
					}
				}

				IGodPowerClient client = playerOrbis.powers().getCurrentPower().getClientHandler();

				Object raytracedObject = client.raytraceObject(playerOrbis);

				if (raytracedObject != null && !client.onRightClickShape(playerOrbis, raytracedObject, event))
				{
					return;
				}
			}

			if (event.getButton() == 1 && !this.painting)
			{
				if (event.isButtonstate())
				{
					this.changingSizeOrigin = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());

					this.changingSize = true;
				}
				else
				{
					this.changingSize = false;
				}
			}
		}
	}

	@Override
	public void clearSelection()
	{
		this.activeSelection = null;
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
