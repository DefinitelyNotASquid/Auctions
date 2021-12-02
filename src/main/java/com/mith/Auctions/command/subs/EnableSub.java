package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.object.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnableSub extends SubCommand {

	public EnableSub() {
		super("enable", Permission.COMMAND_ENABLE, false);
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		boolean enabled = Auctions.getAuctionManager().isAuctionsEnabled();

		String prop;

		if (enabled) {
			prop = "command.auction.enable.already_enabled";
		} else {
			prop = "command.auction.enable.success";
			Auctions.getAuctionManager().setAuctionsEnabled(true);
		}

		sendPropMessage(sender, prop);
	}
}
