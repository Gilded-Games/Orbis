package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.client.godmode.GodPowerBlueprintClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.containers.inventory.InventoryBlueprintForge;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.items.ItemBlueprintPalette;
import com.gildedgames.orbis.common.items.ItemBlueprintStacker;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorBlueprint;
import com.gildedgames.orbis.lib.block.BlockDataContainer;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintData;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis.lib.data.blueprint.BlueprintStackerData;
import com.gildedgames.orbis.lib.data.management.IData;
import com.gildedgames.orbis.lib.data.management.IDataIdentifier;
import com.gildedgames.orbis.lib.data.management.IProject;
import com.gildedgames.orbis.lib.util.mc.StagedInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class GodPowerBlueprint implements IGodPower<GodPowerBlueprintClient>
{

	private final ShapeSelectorBlueprint shapeSelector;

	private final StagedInventory<InventoryBlueprintForge> stagedInventory;

	private Rotation placingRotation = Rotation.NONE;

	private GodPowerBlueprintClient clientHandler;

	private BlueprintData placingBlueprint;

	private BlueprintDataPalette placingPalette;

	private ItemStack previousStack;

	private BlockPos prevPlacingPos;

	private PlayerOrbis playerOrbis;

	public GodPowerBlueprint(final PlayerOrbis playerOrbis, final World world)
	{
		this.playerOrbis = playerOrbis;

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

	public BlueprintStackerData getStackerInHand()
	{
		ItemStack stack = this.playerOrbis.getEntity().inventory.getCurrentItem();

		if (!(stack.getItem() instanceof ItemBlueprintStacker))
		{
			return null;
		}

		return ItemBlueprintStacker.getBlueprintStacker(stack).orElse(null);
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

				final IDataIdentifier id = ItemBlueprint.getBlueprintId(stack);
				Optional<IData> data = OrbisCore.getProjectManager().findData(id);

				if (data.isPresent() && data.get() instanceof BlueprintData)
				{
					this.placingBlueprint = (BlueprintData) data.get();
				}
				else if (player.world.isRemote && id != null)
				{
					Optional<IProject> project = OrbisCore.getProjectManager().findProject(id.getProjectIdentifier());

					if (project.isPresent() && !project.get().getInfo().getMetadata().isDownloaded() && !Minecraft.getMinecraft().isIntegratedServerRunning())
					{
						//TODO: Make fake blueprint to place
					}
				}
			}
			else if (stack.getItem() instanceof ItemBlockDataContainer)
			{
				this.placingPalette = null;
				final Optional<BlockDataContainer> container = ItemBlockDataContainer.getDataContainer(stack);

				container.ifPresent(blockDataContainer -> this.placingBlueprint = new BlueprintData(blockDataContainer));
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

			final Optional<BlockDataContainer> container = ItemBlockDataContainer.getDataContainer(stack);

			container.ifPresent(blockDataContainer -> this.placingBlueprint = new BlueprintData(blockDataContainer));
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
		if (!player.world.isRemote)
		{
			final BlockPos pos = player.getPosition();

			player.openGui(OrbisCore.INSTANCE, OrbisGuiHandler.LOAD_DATA, player.world, pos.getX(), pos.getY(), pos.getZ());
		}
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
	public GodPowerBlueprintClient getClientHandler()
	{
		return this.clientHandler;
	}
}
