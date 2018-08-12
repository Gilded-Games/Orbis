package com.gildedgames.orbis.common.player.godmode;

import com.gildedgames.orbis.client.godmode.GodPowerFrameworkClient;
import com.gildedgames.orbis.client.godmode.IGodPowerClient;
import com.gildedgames.orbis.common.OrbisCore;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.data.BlueprintNode;
import com.gildedgames.orbis.common.items.ItemBlueprint;
import com.gildedgames.orbis.common.items.ItemBlueprintPalette;
import com.gildedgames.orbis.common.network.OrbisGuiHandler;
import com.gildedgames.orbis.common.network.packets.framework.PacketAddNode;
import com.gildedgames.orbis.common.player.godmode.selectors.IShapeSelector;
import com.gildedgames.orbis.common.player.godmode.selectors.ShapeSelectorFramework;
import com.gildedgames.orbis.common.util.OrbisRaytraceHelp;
import com.gildedgames.orbis.common.world_objects.Framework;
import com.gildedgames.orbis_api.data.blueprint.BlueprintData;
import com.gildedgames.orbis_api.data.blueprint.BlueprintDataPalette;
import com.gildedgames.orbis_api.data.framework.FrameworkNode;
import com.gildedgames.orbis_api.data.region.IShape;
import com.gildedgames.orbis_api.util.RegionHelp;
import com.gildedgames.orbis_api.world.WorldObjectUtils;
import net.minecraft.entity.player.EntityPlayer;
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
		BlueprintDataPalette palette = playerOrbis.powers().getBlueprintPower().getPlacingPalette();

		if (data == null && palette == null)
		{
			return;
		}

		final BlockPos pos = OrbisRaytraceHelp.raytraceNoSnapping(playerOrbis.getEntity());

		if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) && !pos.equals(this.getPrevPlacingPos()) && playerOrbis
				.powers().getCurrentPower() == playerOrbis.powers().getFrameworkPower())
		{
			WorldObjectUtils.getIntersectingShape(player.getEntityWorld(), pos)
					.filter(Framework.class::isInstance)
					.map(Framework.class::cast)
					.ifPresent(framework -> {
						FrameworkNode node;

						if (data == null)
						{
							node = new FrameworkNode(new BlueprintNode(palette));
						}
						else
						{
							node = new FrameworkNode(new BlueprintNode(data));
						}

						BlockPos nonRelative = node.getBounds().getMin().toImmutable();

						int xDif = -framework.getPos().getX() - (data != null ? data.getWidth() / 2 : palette.getLargestDim().getWidth() / 2);
						int yDif = -framework.getPos().getY();
						int zDif = -framework.getPos().getZ() - (data != null ? data.getLength() / 2 : palette.getLargestDim().getLength() / 2);

						BlockPos relativePos = pos.add(xDif, yDif, zDif);

						this.prevPlacingPos = relativePos;

						RegionHelp.translate(node.getBounds(), relativePos);

						OrbisCore.network().sendPacketToServer(new PacketAddNode(framework, node, nonRelative));
					});
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

		player.openGui(OrbisCore.INSTANCE, OrbisGuiHandler.LOAD_DATA, player.world, pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public boolean canInteractWithItems(final PlayerOrbis playerOrbis)
	{
		final ItemStack held = playerOrbis.getEntity().getHeldItemMainhand();

		return !held.isEmpty() && !(held.getItem() instanceof ItemBlueprint) && !(held.getItem() instanceof ItemBlueprintPalette);
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
