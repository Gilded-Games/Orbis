package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.client.godmode.GodPowerSelectClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorSelect;
import com.gildedgames.orbis.common.world_objects.WorldShape;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class GodPowerSelect implements IGodPower
{

	private final ShapeSelectorSelect shapeSelector;

	private GodPowerSelectClient clientHandler;

	private WorldShape selectedRegion;

	private int selectedRegionId;

	public GodPowerSelect(final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerSelectClient(this);
		}

		this.shapeSelector = new ShapeSelectorSelect(this);
	}

	@Override
	public void onUpdate(final EntityPlayer player, final PlayerOrbis playerOrbis, final boolean isPowerActive)
	{

	}

	@Override
	public boolean hasCustomGui(PlayerOrbis playerOrbis)
	{
		return false;
	}

	@Override
	public void onOpenGui(final EntityPlayer player)
	{

	}

	@Override
	public boolean canInteractWithItems(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty();
	}

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

	@Override
	public void write(final NBTTagCompound tag)
	{

	}

	@Override
	public void read(final NBTTagCompound tag)
	{

	}

	public WorldShape getSelectedRegion()
	{
		return this.selectedRegion;
	}

	public void setSelectedRegion(final WorldShape region)
	{
		this.selectedRegion = region;
	}

	public int getSelectedRegionId()
	{
		return this.selectedRegionId;
	}

	public void setSelectedRegionId(int id)
	{
		this.selectedRegionId = id;
	}
}
