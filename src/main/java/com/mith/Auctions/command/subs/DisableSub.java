package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.object.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableSub extends SubCommand {

	public DisableSub() {
		super("disable", Permission.COMMAND_DISABLE, false);
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
			prop = "command.auction.disable.success";
			Auctions.getAuctionManager().setAuctionsEnabled(false);
		} else {
			prop = "command.auction.disable.already_disabled";
		}

		sendPropMessage(sender, prop);
	}
}
