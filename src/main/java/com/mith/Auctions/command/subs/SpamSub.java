package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.AuctionsPlayerManager;
import com.mith.Auctions.object.AuctionsPlayer;
import com.mith.Auctions.object.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpamSub extends SubCommand {

	public SpamSub() {
		super("spam", Permission.COMMAND_SPAM, true);
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

		boolean ignoringSpammy = ap.isIgnoringSpammy();

		String prop = "command.auction.spam." + (ignoringSpammy ? "disabled" : "enabled");

		sendPropMessage(player, prop);
		ap.setIgnoringSpammy(!ignoringSpammy);
	}
}
