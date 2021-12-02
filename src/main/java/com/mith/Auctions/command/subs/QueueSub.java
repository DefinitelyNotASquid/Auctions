package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.AuctionManager;
import com.mith.Auctions.object.Auction;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class QueueSub extends SubCommand {

	public QueueSub() {
		super("queue", Permission.COMMAND_QUEUE, true, "q");
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		AuctionManager manager = Auctions.getAuctionManager();
		List<Auction> queueList = manager.getQueue();

		if (queueList.isEmpty()) {
			sendPropMessage(sender, "command.no_queued_auctions");
			return;
		}
		for (Auction auc : queueList)
		{
			BaseComponent[] comp = auc.getAuctionInfo();
			MessageUtil.privateMessage(sender, comp);
		}
	}
}
