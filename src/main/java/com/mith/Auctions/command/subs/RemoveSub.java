package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.util.RewardUtil;
import com.mith.Auctions.object.Auction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveSub extends SubCommand {

	public RemoveSub() {
		super("remove", Permission.COMMAND_REMOVE, true, "r");
	}

	@Override
	public void run(CommandSender sender, String[] args) {

		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		Auction auction = Auctions.getAuctionManager().removeFromQueue(player.getUniqueId());

		if (auction == null) {
			sendPropMessage(player, "command.auction.remove.not_in_queue");
			return;
		}

		sendPropMessage(player, "command.auction.remove.success");
		RewardUtil.rewardCancel(auction);
	}
}
