package com.mith.Auctions.manager;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.util.RewardUtil;
import com.mith.Auctions.event.AuctionStartEvent;
import com.mith.Auctions.object.Auction;
import com.mith.Auctions.object.AuctionsPlayer;
import com.mith.Auctions.runnable.AuctionRunnable;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionManager {

	private Auctions plugin;

	private AuctionRunnable currentRunnable;
	private List<Auction> queue;

	private boolean auctionsEnabled = true, inDelayedTask = false;

	public AuctionManager(Auctions plugin) {
		this.plugin = plugin;
		this.queue = new ArrayList<>();
	}

	public int getQueueSize() {
		return queue.size();
	}

	public List<Auction> getQueue() {
		return queue;
	}

	public boolean addToQueue(Auction auction) {
		if (getCurrentRunnable() == null && !inDelayedTask) {
			AuctionStartEvent event = new AuctionStartEvent(auction);
			Bukkit.getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				currentRunnable = new AuctionRunnable(auction, plugin);
			}

			return false;
		} else {
			queue.add(auction);
			return true;
		}
	}

	public boolean inQueueOrCurrent(UUID auctioneer) {
		for (Auction auc : queue) {
			if (auc.getAuctioneer().getUniqueId().equals(auctioneer))
				return true;
		}

		return getCurrentAuction() != null && getCurrentAuction().getAuctioneer().getUniqueId().equals(auctioneer);
	}

	public int getPlayerQueueSize(UUID auctioneer){
		int count = 0;
		for (Auction auc : queue) {
			if (auc.getAuctioneer().getUniqueId().equals(auctioneer)){
				count++;
			}
		}
		if(getCurrentAuction() != null && getCurrentAuction().getAuctioneer().getUniqueId().equals(auctioneer)){
			count++;
		}
		return count;
	}

	public int getPositionInQueue(AuctionsPlayer ap) {
		if (queue.isEmpty())
			return -1;

		for (int i = 0; i < queue.size(); i++) {
			Auction auc = queue.get(i);

			if (auc.getAuctioneer().getUniqueId().equals(ap.getUniqueId()))
				return i + 1;
		}

		return -1;
	}

	public Auction removeFromQueue(UUID auctioneer) {
		Auction auction = null;

		for (Auction auc : queue) {
			if (auc.getAuctioneer().getUniqueId().equals(auctioneer)) {
				auction = auc;
				break;
			}
		}

		if (auction != null) {
			queue.remove(auction);
		}

		return auction;
	}

	public void next() {
		currentRunnable = null;

		if (!isAuctionsEnabled())
			return;

		if (queue.isEmpty())
			return;

		Auction auction = queue.get(0);

		queue.remove(0);

		AuctionStartEvent event = new AuctionStartEvent(auction);
		Bukkit.getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			long delay = 20 * ConfigManager.getConfig().getLong("general.time-between");

			inDelayedTask = true;

			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				currentRunnable = new AuctionRunnable(auction, plugin);
				inDelayedTask = false;
			}, delay);
		}
	}

	public AuctionRunnable getCurrentRunnable() {
		return currentRunnable;
	}

	public Auction getCurrentAuction() {
		if (getCurrentRunnable() == null)
			return null;

		return getCurrentRunnable().getAuction();
	}

	public boolean isAuctionsEnabled() {
		return auctionsEnabled;
	}

	public void setAuctionsEnabled(boolean auctionsEnabled) {
		this.auctionsEnabled = auctionsEnabled;
	}

	public void disabling() {
		if (getCurrentRunnable() != null) {
			getCurrentRunnable().cancelAuction();
		}

		for (Auction auc : queue) {
			RewardUtil.rewardCancel(auc);
		}

		queue.clear();
	}
}
