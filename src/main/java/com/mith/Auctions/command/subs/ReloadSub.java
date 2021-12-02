package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.manager.Mappings;
import com.mith.Auctions.manager.Messages;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.command.AuctionCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadSub extends SubCommand {

	public ReloadSub() {
		super("reload", Permission.COMMAND_RELOAD, false);
	}

	public void run(CommandSender sender, String[] args) {

		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		ConfigManager.getInstance().reloadConfiguration();
		Messages.getInstance().reload();
		Mappings.getInstance().reloadConfiguration();

		if ((Bukkit.getPluginManager().getPlugin("auctions")) != null) {
			((Auctions) Bukkit.getPluginManager().getPlugin("auctions")).getCommand("auctions")
                    .setExecutor(new AuctionCommand());
		}

		sendPropMessage(sender, "command.auction.reload");
	}
}
