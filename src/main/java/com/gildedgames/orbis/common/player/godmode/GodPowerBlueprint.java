package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.api.block.BlockDataContainer;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingDataException;
import com.gildedgames.orbis.api.core.exceptions.OrbisMissingProjectException;
import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis.api.data.management.IDataIdentifier;
import com.gildedgames.orbis.api.util.mc.StagedInventory;
import com.gildedgames.orbis.client.godmode.GodPowerBlueprintClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.inventory.InventoryBlueprintForge;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.items.ItemBlueprintPalette;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GodPowerBlueprint implements IGodPower
{

	private final ShapeSelectorBlueprint shapeSelector;

	private final StagedInventory<InventoryBlueprintForge> stagedInventory;

	private Rotation placingRotation = Rotation.NONE;

	private GodPowerBlueprintClient clientHandler;

	private BlueprintData placingBlueprint;

	private BlueprintDataPalette placingPalette;

	private ItemStack previousStack;

	private BlockPos prevPlacingPos;

	public GodPowerBlueprint(final PlayerOrbis playerOrbis, final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerBlueprintClient(this);
		}

		this.shapeSelector = new ShapeSelectorBlueprint(this);
		this.stagedInventory = new StagedInventory<>(playerOrbis.getEntity(), () -> new InventoryBlueprintForge(playerOrbis.getEntity()),
				m -> PlayerOrbis.get(m).powers().getBlueprintPower().getStagedInventory(), "blueprintForge");
	}

	public BlockPos getPrevPlacingPos()
	{
		return this.prevPlacingPos;
	}

	public void setPrevPlacingPos(final BlockPos pos)
	{
		this.prevPlacingPos = pos;
	}

	public StagedInventory<InventoryBlueprintForge> getStagedInventory()
	{
		return this.stagedInventory;
	}

	public Rotation getPlacingRotation()
	{
		return this.placingRotation;
	}

	public void setPlacingRotation(final Rotation rotation)
	{
		this.placingRotation = rotation;
	}

	public BlueprintDataPalette getPlacingPalette()
	{
		return this.placingPalette;
	}

	public BlueprintData getPlacingBlueprint()
	{
		return this.placingBlueprint;
	}

	public IInventory getForgeInventory()
	{
		return this.stagedInventory.get();
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
	public void onUpdate(final EntityPlayer player, final PlayerOrbis playerOrbis, final boolean isPowerActive)
	{
		final ItemStack stack = player.getHeldItemMainhand();

		if (this.previousStack != stack)
		{
			this.previousStack = stack;

			if (stack.getItem() instanceof ItemBlueprintPalette)
			{
				this.placingBlueprint = null;

				this.placingPalette = ItemBlueprintPalette.getBlueprintPalette(stack);
			}
			else if (stack.getItem() instanceof ItemBlueprint)
			{
				this.placingPalette = null;
				this.placingBlueprint = null;

				try
				{
					final IDataIdentifier id = ItemBlueprint.getBlueprintId(stack);
					this.placingBlueprint = OrbisCore.getProjectManager().findData(id);
				}
				catch (final OrbisMissingDataException | OrbisMissingProjectException e)
				{
					OrbisCore.LOGGER.error(e);
					player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
				}
			}
			else if (stack.getItem() instanceof ItemBlockDataContainer)
			{
				this.placingPalette = null;
				final BlockDataContainer container = ItemBlockDataContainer.getDataContainer(stack);

				if (container != null)
				{
					this.placingBlueprint = new BlueprintData(container);
				}
			}
			else
			{
				this.placingPalette = null;
				this.placingBlueprint = null;
			}
		}
		else if (stack.getItem() instanceof ItemBlueprintPalette && this.placingPalette == null)
		{
			this.placingBlueprint = null;

			this.placingPalette = ItemBlueprintPalette.getBlueprintPalette(stack);
		}
		else if (stack.getItem() instanceof ItemBlockDataContainer && this.placingBlueprint == null)
		{
			this.placingPalette = null;

			final BlockDataContainer container = ItemBlockDataContainer.getDataContainer(stack);

			if (container != null)
			{
				this.placingBlueprint = new BlueprintData(container);
			}
		}
	}

	@Override
	public boolean hasCustomGui(PlayerOrbis playerOrbis)
	{
		return true;
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{
		final BlockPos pos = player.getPosition();

		player.openGui(OrbisCore.INSTANCE, OrbisGuiHandler.ORBIS_BLUEPRINT_LOAD, player.world, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean canInteractWithItems(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty();
	}

	@Nullable
	@Override
	public IShapeSelector getShapeSelector()
	{
		return this.shapeSelector;
	}

	@Override
	public IGodPowerClient getClientHandler()
	{
		return this.clientHandler;
	}
}
