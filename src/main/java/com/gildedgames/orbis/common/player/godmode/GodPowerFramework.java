package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.api.data.blueprint.BlueprintData;
import com.gildedgames.orbis.api.data.framework.FrameworkNode;
import com.gildedgames.orbis.api.data.region.IShape;
import com.gildedgames.orbis.api.world.IWorldObjectGroup;
import com.gildedgames.orbis.api.world.WorldObjectManager;
import com.gildedgames.orbis.client.godmode.GodPowerFrameworkClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.data.BlueprintNode;
import com.gildedgames.orbis.common.data.BlueprintPalette;
import com.gildedgames.orbis.common.items.ItemBlockDataContainer;
import com.gildedgames.orbis.common.network.NetworkingOrbis;
import com.gildedgames.orbis.common.network.packets.framework.PacketAddNode;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorFramework;
import com.gildedgames.orbis.common.util.RaytraceHelp;
import com.gildedgames.orbis.common.world_objects.Framework;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

public class GodPowerFramework implements IGodPower
{

	private final ShapeSelectorFramework shapeSelector;

	private GodPowerFrameworkClient clientHandler;

	private BlockPos prevPlacingPos;

	public GodPowerFramework(final World world)
	{
		if (world.isRemote)
		{
			this.clientHandler = new GodPowerFrameworkClient();
		}

		this.shapeSelector = new ShapeSelectorFramework(this);
	}

	public BlockPos getPrevPlacingPos()
	{
		return this.prevPlacingPos;
	}

	public void setPrevPlacingPos(final BlockPos pos)
	{
		this.prevPlacingPos = pos;
	}

	@Override
	public void onUpdate(final EntityPlayer player, final PlayerOrbis playerOrbis, final boolean isPowerActive)
	{
		if (!player.world.isRemote)
		{
			return;
		}

		BlueprintData data = playerOrbis.powers().getBlueprintPower().getPlacingBlueprint();
		BlueprintPalette palette = playerOrbis.powers().getBlueprintPower().getPlacingPalette();

		if (data == null && palette == null)
		{
			return;
		}

		final BlockPos pos = RaytraceHelp.doOrbisRaytrace(playerOrbis, playerOrbis.raytraceWithRegionSnapping());

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && !pos.equals(this.getPrevPlacingPos()) && playerOrbis
				.powers().getCurrentPower() == playerOrbis.powers().getFrameworkPower())
		{
			final WorldObjectManager manager = WorldObjectManager.get(player.world);
			final IWorldObjectGroup group = manager.getGroup(0);

			IShape shape = group.getIntersectingShape(pos);

			if (shape instanceof Framework)
			{
				Framework framework = (Framework) shape;

				FrameworkNode node;

				if (data == null)
				{
					node = new FrameworkNode(new BlueprintNode(palette));
				}
				else
				{
					node = new FrameworkNode(new BlueprintNode(data));
				}

				int xDif = -framework.getPos().getX() - (data != null ? data.getWidth() / 2 : palette.getLargestInArea().getWidth() / 2);
				int yDif = -framework.getPos().getY();
				int zDif = -framework.getPos().getZ() - (data != null ? data.getLength() / 2 : palette.getLargestInArea().getLength() / 2);

				BlockPos relativePos = pos.add(xDif, yDif, zDif);

				this.prevPlacingPos = relativePos;

				NetworkingOrbis.sendPacketToServer(new PacketAddNode(framework, node, relativePos));
			}
		}
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

		return !held.isEmpty() && (held.getItem() instanceof ItemBlock || held.getItem() instanceof ItemBlockDataContainer);
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

}
