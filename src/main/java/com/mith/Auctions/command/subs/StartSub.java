package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.AuctionManager;
import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.object.Permission;
import com.mith.Auctions.event.AuctionQueueEvent;
import com.mith.Auctions.manager.AuctionsPlayerManager;
import com.mith.Auctions.object.Auction;
import com.mith.Auctions.object.AuctionsPlayer;
import com.mith.Auctions.util.AuctionUtil;
import com.mith.Auctions.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartSub extends SubCommand {

	public StartSub() {
		super("start", Permission.COMMAND_START, true, "s");
	}

	public void run(CommandSender sender, String[] args) {

		Player player = (Player)sender;
		if(Auctions.bannedPlayers.contains(player.getUniqueId()))
		{
			sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
			return;
		}

		if (args.length < 3 || args.length > 6) {
			sendPropMessage(player, "command.auction.start.help");
			return;
		}

		AuctionManager manager = Auctions.getAuctionManager();

		if (!manager.isAuctionsEnabled()) {
			sendPropMessage(player, "command.auction.start.disabled");
			return;
		}

		if (manager.getQueueSize() == ConfigManager.getConfig().getInt("general.auction-queue-limit")) {
			sendPropMessage(player, "command.auction.start.queue_full");
			return;
		}

		if(ConfigManager.getConfig().getInt("general.auction-player-queue-limit") > 0) {


			int currentPlayerAuctionCount = manager.getPlayerQueueSize(player.getUniqueId());

			if(ConfigManager.getConfig().getInt("general.auction-player-queue-limit") <= currentPlayerAuctionCount ){
				sendPropMessage(player, "command.auction.start.in_queue");
				return;
			}
		}

		if (!hasFee(player)) {
			sendPropMessage(player, "command.auction.start.lacking_fee");
			return;
		}

		AuctionsPlayer ap = AuctionsPlayerManager.getInstance().getPlayer(player.getUniqueId());

		Auction auction = AuctionUtil.parseAuction(
				ap,
				args[1],
				args[2],
				args.length < 4 ? String
						.valueOf(ConfigManager.getInstance().get("auctions.default.increment")) : args[3],
				args.length < 5 ? String.valueOf(ConfigManager.getInstance().get("auctions.default.autobuy")) :
						args[4],
				args.length < 6 ? String
						.valueOf(ConfigManager.getConfig().getInt("auctions.default.auction-time")) : args[5],
				false);

		if (auction == null)
			return;

		AuctionQueueEvent event = new AuctionQueueEvent(auction);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;

		removeFee(player);

		ItemUtil.removeItemsFromInv(auction, player);

		if (Auctions.getAuctionManager().addToQueue(auction)) {
			int position = Auctions.getAuctionManager().getQueueSize();
			sendPropMessage(player, "command.auction.start.added_to_queue", position);
		}
	}

	private boolean hasFee(Player p) {
		double fee = ConfigManager.getConfig().getDouble("auctions.fees.start-price");

		return Auctions.getEcon().has(p, fee);
	}

	private void removeFee(Player p) {
		double fee = ConfigManager.getConfig().getDouble("auctions.fees.start-price");

		if (fee > 0) {
			Auctions.getEcon().withdrawPlayer(p, fee);
		}
	}
}
