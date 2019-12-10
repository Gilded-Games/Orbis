package com.gildedgames.orbis.common;

import com.gildedgames.orbis.common.blocks.BlocksOrbis;
import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import com.gildedgames.orbis.common.world_actions.WorldActionLogs;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlockDestroy;
import com.gildedgames.orbis.common.world_actions.impl.WorldActionBlockPlace;
import com.gildedgames.orbis.lib.block.BlockData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber()
public class OrbisDeveloperModeEvents
{

	@SubscribeEvent
	public static void onWorldSaved(final WorldEvent.Save event)
	{
		OrbisCore.saveDataCache();
	}

	@SubscribeEvent
	public static void onCommandEvent(final CommandEvent event)
	{
		if (event.getCommand() instanceof CommandGameMode)
		{
			final String[] args = event.getParameters();
			if (args.length == 0)
			{
				event.setException(new WrongUsageException("commands.gamemode.usage"));
				return;
			}

			final String gamemodeString = args[0];
			boolean setsDeveloperMode = false;

			if (gamemodeString.equals("designer") || "designer".startsWith(gamemodeString))
			{
				setsDeveloperMode = true;
			}
			else
			{
				try
				{
					final int gamemode = CommandBase.parseInt(gamemodeString, 0, GameType.values().length - 1);

					if (gamemode == 4)
					{
						setsDeveloperMode = true;
					}
				}
				catch (final NumberInvalidException e)
				{
					return;
				}
			}

			try
			{
				final EntityPlayer player = args.length >= 2 ?
						CommandBase.getPlayer(event.getSender().getServer(), event.getSender(), args[1]) :
						CommandBase.getCommandSenderAsPlayer(event.getSender());

				if (setsDeveloperMode)
				{
					PlayerOrbis.get(player).setDeveloperMode(true);
					player.setGameType(GameType.CREATIVE);

					final ITextComponent itextcomponent = new TextComponentTranslation("gameMode.designer");

					event.setCanceled(true);

					if (event.getSender().getEntityWorld().getGameRules().getBoolean("sendCommandFeedback"))
					{
						player.sendMessage(new TextComponentTranslation("gameMode.changed", itextcomponent));
					}

					if (player == event.getSender())
					{
						CommandBase.notifyCommandListener(event.getSender(), event.getCommand(), 1, "commands.gamemode.success.self",
								itextcomponent);
					}
					else
					{
						CommandBase.notifyCommandListener(event.getSender(), event.getCommand(), 1, "commands.gamemode.success.other",
								player.getName(), itextcomponent);
					}
				}
				else
				{
					PlayerOrbis.get(player).setDeveloperMode(false);
				}
			}
			catch (final CommandException e)
			{
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerBreak(BlockEvent.BreakEvent event)
	{
		if (event.getState() == BlocksOrbis.orbis_floor.getDefaultState())
		{
			event.setCanceled(true);
			return;
		}

		EntityPlayer player = event.getPlayer();
		PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (playerOrbis == null || !playerOrbis.inDeveloperMode())
		{
			return;
		}

		BlockPos pos = event.getPos();
		World world = event.getWorld();

		NBTTagCompound teData = null;
		TileEntity te = world.getTileEntity(pos);

		if (te != null)
		{
			teData = new NBTTagCompound();
			te.writeToNBT(teData);
		}

		IBlockState state = event.getState();

		BlockData blockData = teData == null ? new BlockData(state) : new BlockData(state, teData);

		playerOrbis.getWorldActionLog(WorldActionLogs.BLOCKS).apply(world, new WorldActionBlockDestroy(blockData, pos));
	}

	@SubscribeEvent
	public static void onPlayerPlace(BlockEvent.PlaceEvent event)
	{
		EntityPlayer player = event.getPlayer();
		PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (playerOrbis == null || !playerOrbis.inDeveloperMode())
		{
			return;
		}

		BlockPos pos = event.getPos();
		World world = event.getWorld();

		NBTTagCompound beforeTe = null;
		TileEntity te = world.getTileEntity(pos);

		if (te != null)
		{
			beforeTe = new NBTTagCompound();
			te.writeToNBT(beforeTe);
		}

		NBTTagCompound afterTe = event.getBlockSnapshot().getNbt();

		IBlockState afterState = event.getBlockSnapshot().getCurrentBlock();
		IBlockState beforeState = event.getBlockSnapshot().getReplacedBlock();

		BlockData before = beforeTe == null ? new BlockData(beforeState) : new BlockData(beforeState, beforeTe);
		BlockData after = afterTe == null ? new BlockData(afterState) : new BlockData(afterState, afterTe);

		playerOrbis.getWorldActionLog(WorldActionLogs.BLOCKS).apply(world, new WorldActionBlockPlace(before, after, pos));
	}

	/**
	 * Prevents players in Developer Mode from right clicking
	 * blocks or interacting with them.
	 * @param event
	 */
	@SubscribeEvent
	public static void onPlayerInteract(final PlayerInteractEvent.RightClickBlock event)
	{
		final EntityPlayer player = event.getEntityPlayer();
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (!playerOrbis.canInteractWithItems())
		{
			event.setCanceled(true);
		}
	}

	/**
	 * Prevents players in Developer Mode from right clicking
	 * with items in their hand and activating their interact
	 * function.
	 * @param event
	 */
	@SubscribeEvent
	public static void onPlayerInteract(final PlayerInteractEvent.RightClickItem event)
	{
		final EntityPlayer player = event.getEntityPlayer();
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (!playerOrbis.canInteractWithItems())
		{
			event.setCanceled(true);
		}
	}

	/**
	 * Prevents players in Developer Mode from placing blocks.
	 * @param event
	 */
	@SubscribeEvent
	public static void onPlayerPlacesBlock(final BlockEvent.PlaceEvent event)
	{
		final EntityPlayer player = event.getPlayer();
		final PlayerOrbis playerOrbis = PlayerOrbis.get(player);

		if (!playerOrbis.canInteractWithItems())
		{
			event.setCanceled(true);
		}
	}

}
