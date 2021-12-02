package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.AuctionsPlayerManager;
import com.mith.Auctions.object.AuctionsPlayer;
import com.mith.Auctions.object.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnoreSub extends SubCommand {

	public IgnoreSub() {
		super("ignore", Permission.COMMAND_IGNORE, true);
	}

	@Override
	public void run(CommandSender sender, String[] args) {

		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		AuctionsPlayer ap = AuctionsPlayerManager.getInstance().getPlayer(player.getUniqueId());

		boolean ignoringAll = ap.isIgnoringAll();

		String prop = "command.auction.ignore." + (ignoringAll ? "disabled" : "enabled");

		sendPropMessage(player, prop);
		ap.setIgnoringAll(!ignoringAll);
	}
}
