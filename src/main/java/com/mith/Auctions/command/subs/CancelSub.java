package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.event.AuctionCancelEvent;
import com.mith.Auctions.object.Auction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CancelSub extends SubCommand {

	public CancelSub() {
		super("cancel", Permission.COMMAND_CANCEL, false, "c");
	}

	@Override
	public void run(CommandSender sender, String[] args) {

		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		Auction current = Auctions.getAuctionManager().getCurrentAuction();

		if (current == null) {
			sendPropMessage(sender, "command.no_current_auction");
			return;
		}

		if (!sender.hasPermission(Permission.COMMAND_CANCEL_OTHERS.toString())) {

			if (!player.getUniqueId().equals(current.getAuctioneer().getUniqueId())) {
				sendPropMessage(player, "command.auction.cancel.not_yours");
				return;
			}

			int minTime = ConfigManager.getConfig().getInt("general.minimum-cancel-time");

			if (current.getAuctionTime() < minTime) {
				sendPropMessage(player, "command.auction.cancel.too_late");
				return;
			}
		}

		AuctionCancelEvent event = new AuctionCancelEvent(current, sender);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		Auctions.getAuctionManager().getCurrentRunnable().cancelAuction();
	}
}
