package com.gildedgames.orbis.common.network;

import com.gildedgames.orbis.common.capabilities.player.PlayerOrbis;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandActivateDesignerGamemode implements ICommand
{
	private final List<String> aliases;

	public CommandActivateDesignerGamemode()
	{
		this.aliases = Collections.emptyList();
	}

	@Override
	public String getName()
	{
		return "toggledesigner";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/toggledesigner [player]";
	}

	@Override
	public List<String> getAliases()
	{
		return this.aliases;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		try
		{
			final EntityPlayer player = args.length >= 1 ?
					CommandBase.getPlayer(server, sender, args[0]) :
					CommandBase.getCommandSenderAsPlayer(sender);

			PlayerOrbis playerOrbis = PlayerOrbis.get(player);

			if (playerOrbis == null)
			{
				return;
			}

			if (!playerOrbis.inDeveloperMode())
			{
				playerOrbis.setDeveloperMode(true);
				player.setGameType(GameType.CREATIVE);

				final ITextComponent itextcomponent = new TextComponentTranslation("gameMode.designer");

				if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback"))
				{
					player.sendMessage(new TextComponentTranslation("gameMode.changed", itextcomponent));
				}

				if (player == sender)
				{
					CommandBase.notifyCommandListener(sender, this, 1, "commands.gamemode.success.self",
							itextcomponent);
				}
				else
				{
					CommandBase.notifyCommandListener(sender, this, 1, "commands.gamemode.success.other",
							player.getName(), itextcomponent);
				}
			}
			else
			{
				playerOrbis.setDeveloperMode(false);

				final ITextComponent itextcomponent = new TextComponentTranslation("gameMode.creative");

				itextcomponent.getStyle().setColor(TextFormatting.GRAY).setItalic(true);

				if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback"))
				{
					player.sendMessage(new TextComponentTranslation("gameMode.changed", itextcomponent));
				}
			}
		}
		catch (final CommandException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return sender.canUseCommand(2, this.getName());
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
	{
		return args.length == 1 ? CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.emptyList();
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index)
	{
		return index == 0;
	}

	@Override
	public int compareTo(ICommand o)
	{
		return this.getName().compareTo(o.getName());
	}
}
