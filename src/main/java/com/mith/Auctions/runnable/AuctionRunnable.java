package com.mith.Auctions.runnable;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.event.AuctionEndEvent;
import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.manager.Messages;
import com.mith.Auctions.object.AverageItem;
import com.mith.Auctions.util.MessageUtil;
import com.mith.Auctions.util.RewardUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.milkbowl.vault.economy.Economy;
import com.mith.Auctions.object.Auction;
import com.mith.Auctions.object.Bidder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuctionRunnable extends BukkitRunnable {

    private Auction auction;
    private UUID auctioneer;
    private int timeLeft;
    private List<Integer> broadcastTimes = ConfigManager.getConfig().getIntegerList("auctions.broadcast-times");
    private int antiSnipeTimesRun = 0;

    public AuctionRunnable(Auction auction, Auctions plugin) {
        this.auction = auction;
        this.auctioneer = auction.getAuctioneer().getUniqueId();
        this.timeLeft = auction.getAuctionTime();

        broadcastStart();
        runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        if (broadcastTimes.contains(timeLeft)) {
            broadcastTime();
        }

        if (timeLeft == 0) {
            endAuction();
            return;
        }

        timeLeft--;
        getAuction().setAuctionTime(timeLeft);
    }

    private void broadcastStart() {
        BaseComponent[] comp = getAuction().getStartingMessage();

        MessageUtil.broadcastRegular(auctioneer, comp);
    }

    private void broadcastTime() {
        double currentAmount = auction.anyBids() ? auction.getBiddersHighestToLowest().get(0).getAmount() :
                auction.getStartingPrice();
        String broadcast = Messages.getInstance().getStringWithoutColoring("auction.time_left", timeLeft,
                "%item%", auction.getAmount(), auction.anyBids() ? auction.getLastBidder().getAmount() : auction.getStartingPrice());

        MessageUtil.broadcastSpammy(auctioneer, auction.formatMessage(broadcast));
    }

    public Auction getAuction() {
        return auction;
    }

    public int getAntiSnipeTimesRun() {
        return antiSnipeTimesRun;
    }

    public void antiSnipe() {
        FileConfiguration data = ConfigManager.getConfig();

        if (!(getAntiSnipeTimesRun() < data.getInt("antisnipe.run-times")))
            return;

        int addTime = data.getInt("antisnipe.time");

        MessageUtil.broadcastSpammy(auctioneer, "auction.antisnipe", addTime);
        timeLeft += addTime;

        antiSnipeTimesRun++;
    }

    public void endAuction() {
        cancel();

        AuctionEndEvent event = new AuctionEndEvent(getAuction());
        Bukkit.getPluginManager().callEvent(event);

        Auctions.getAuctionManager().next();

        if (getAuction().anyBids()) {
            Auction auc = getAuction();
            Economy econ = Auctions.getEcon();

            Bidder lastBidder = auc.getLastBidder();

            String lastBidderName = lastBidder.getBidder().getOfflinePlayer().getName();
            double lastBidAmount = lastBidder.getAmount();

            String broadcast = Messages.getInstance().getStringWithoutColoring("auction.finish", lastBidderName,
                    lastBidAmount, "%item%", auc.getAmount());

            MessageUtil.broadcastRegular(auctioneer, auc.formatMessage(broadcast));

            RewardUtil.rewardAuction(auc, econ);

            updateAverageStats(auc);

        } else {
            String broadcast = Messages.getInstance().getStringWithoutColoring("auction.finish.no_bids", "%item%",
                    getAuction().getAmount());

            MessageUtil.broadcastRegular(auctioneer, getAuction().formatMessage(broadcast));
            RewardUtil.rewardCancel(getAuction());
        }
    }

    public void updateAverageStats(Auction auc)
    {
        Material m = auc.getItem().getType();

        Optional<AverageItem> avg =  Auctions.getAverageItemsList().stream().filter(averageItem -> averageItem.getMaterial() == m).findFirst();

        if(!avg.isPresent())
        {
            System.out.println("[Auctions] WARNING: Encountered an error where auctions could not update the stats for the average item. ");
        }

        AverageItem averageItem = avg.get();
        averageItem.updateAmount(auc.getAmount());
        averageItem.updatePrice((auc.getLastBidder().getAmount()));

        List<AverageItem> avglist = Auctions.getAverageItemsList();

        Iterator itr = avglist.iterator();
        while (itr.hasNext())
        {
            AverageItem x = (AverageItem)itr.next();
            if (x.getMaterial() == m)
                itr.remove();
        }

        avglist.add(averageItem);
        Auctions.setAverageItemsList(avglist);
    }

    public void cancelAuction() {
        cancel();

        MessageUtil.broadcastRegular(auctioneer, "auction.cancelled");
        Auctions.getAuctionManager().next();

        RewardUtil.rewardCancel(getAuction());
    }

    public void impoundAuction(Player impounder) {
        cancel();

        MessageUtil.broadcastRegular(auctioneer, "auction.impounded");
        Auctions.getAuctionManager().next();

        RewardUtil.rewardImpound(getAuction(), impounder);
    }
}
