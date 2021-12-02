package com.mith.Auctions.command;

import com.mith.Auctions.command.subs.*;
import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.manager.Messages;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.util.MessageUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AuctionCommand implements CommandExecutor {

	private List<SubCommand> subs = new ArrayList<>();

	public AuctionCommand() {
		registerSubs();
	}

	private void registerSubs() {
		subs.add(new AverageSub());
		subs.add(new BlacklistSub());
		subs.add(new CancelSub());
		subs.add(new DisableSub());
		subs.add(new EnableSub());
		subs.add(new IgnoreSub());
		subs.add(new IgnorePlayerSub());
		subs.add(new ImpoundSub());
		subs.add(new InfoSub());
		subs.add(new QueueSub());
		subs.add(new ReloadSub());
		subs.add(new RemoveSub());
		subs.add(new SpamSub());
		subs.add(new StartSub());

		if (ConfigManager.getConfig().getBoolean("sealed-auctions.enabled")) {
			subs.add(new StartSealedSub());
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!hasPermission(sender))
			return true;

		if (args.length == 0)
			return help(sender);

		SubCommand sub = findSub(args[0]);

		if (sub == null) {
			sendPropMessage(sender, "command.invalid_sub");
			return true;
		}

		if (!sender.hasPermission(sub.getPermission())) {
			sendPropMessage(sender, "command.no_perm");
			return true;
		}

		if (sub.isPlayerOnly() && !(sender instanceof Player)) {
			sendPropMessage(sender, "command.player_only");
			return true;
		}

		if(sender instanceof Player){

			Player player = (Player)sender;

			if(player.getGameMode().equals(GameMode.CREATIVE) && !player.hasPermission("auctions.admin")){
				sendPropMessage(sender, "command.in_creative");
				return true;
			}

		}

		sub.run(sender, args);

		return true;
	}


	private boolean hasPermission(CommandSender sender) {
		if (sender.hasPermission(Permission.COMMAND_BASE.toString()))
			return true;

		sendPropMessage(sender, "command.no_perm");

		return false;
	}

	private boolean help(CommandSender sender) {
		boolean player = sender instanceof Player;

		MessageUtil.privateMessage(sender, "command.help");

		for (SubCommand sub : subs) {
			boolean canUse =
					player && sub.isPlayerOnly() || player && !sub.isPlayerOnly() || !(!player && sub.isPlayerOnly());

			if (sender.hasPermission(sub.getPermission()) && canUse) {
				MessageUtil.privateMessage(sender, sub.getHelpProperty());
			}
		}

		if (sender.hasPermission(Permission.COMMAND_BID.toString())) {
			MessageUtil.privateMessage(sender, "command.bid.help");
		}

		return true;
	}

	private SubCommand findSub(String arg) {
		for (SubCommand sub : subs)
			if (sub.matchSub(arg))
				return sub;

		return null;
	}

	private void sendPropMessage(CommandSender sender, String property) {
		String message = Messages.getString(property);

		if (sender instanceof Player)
			sender.sendMessage(message);
		else
			sender.sendMessage(ChatColor.stripColor(message));
	}
}
