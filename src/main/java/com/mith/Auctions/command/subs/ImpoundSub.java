package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.event.AuctionImpoundEvent;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.object.Auction;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ImpoundSub extends SubCommand {

	public ImpoundSub() {
		super("impound", Permission.COMMAND_IMPOUND, true);
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

		AuctionImpoundEvent event = new AuctionImpoundEvent(current, player);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		sendPropMessage(player, "command.auction.impound");
		Auctions.getAuctionManager().getCurrentRunnable().impoundAuction(player);
	}
}
