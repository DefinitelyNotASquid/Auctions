package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import com.mith.Auctions.object.Auction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoSub extends SubCommand {

	public InfoSub() {
		super("info", Permission.COMMAND_INFO, false, "i");
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

		BaseComponent[] comp = current.getStartingMessage();
		MessageUtil.privateMessage(sender, comp);
	}
}
