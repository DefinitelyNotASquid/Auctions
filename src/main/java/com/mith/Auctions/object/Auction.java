package com.mith.Auctions.object;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.manager.Messages;
import com.mith.Auctions.util.MessageUtil;
import com.mith.Auctions.util.ReflectionUtil;
import com.mith.UnicodeApi.UnicodeApi;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Auction {

    private AuctionsPlayer auctioneer;
    private ItemStack item;
    private int amount, auctionTime;
    private double starting, increment, autoBuy;
    private boolean isSealed;
    private List<Bidder> bidders;

    public Auction(AuctionsPlayer auctioneer, ItemStack item, int amount, int auctionTime, double starting,
                   double increment, double autoBuy, boolean isSealed) {
        this.auctioneer = auctioneer;
        this.item = item;
        this.amount = amount;
        this.auctionTime = auctionTime;
        this.starting = starting;
        this.increment = increment;
        this.autoBuy = autoBuy;
        this.isSealed = isSealed;
        bidders = new ArrayList<>();
    }

    public AuctionsPlayer getAuctioneer() {
        return auctioneer;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getAmount() {
        return amount;
    }

    public int getAuctionTime() {
        return auctionTime;
    }

    public void setAuctionTime(int auctionTime) {
        this.auctionTime = auctionTime;
    }

    public double getStartingPrice() {
        return starting;
    }

    public double getIncrement() {
        return increment;
    }

    public double getAutoBuy() {
        return autoBuy;
    }

    public boolean isSealed() {
        return isSealed;
    }

    public void addNewBidder(Bidder b) {
        updateConsecutiveBidder(b, b.getAmount());
        bidders.add(b);
        broadcastBid(b);
    }

    public void updateBidder(Bidder b) {
        broadcastBid(b);
    }

    public void updateConsecutiveBidder(Bidder upcomingLatestBidder, double upcomingAmount) {
        Bidder lastBidder = getLastBidder();

        if (lastBidder == null)
            return;

        if (lastBidder == upcomingLatestBidder)
            return;

        lastBidder.resetConsecutiveBids();
    }

    public void broadcastBid(Bidder b) {
        if (getAutoBuy() != 0 && b.getAmount() == getAutoBuy()) {
            Auctions.getAuctionManager().getCurrentRunnable().endAuction();
            return;
        }

        if (!isSealed()) {
            String bidder = b.getBidder().getOnlinePlayer().getName();
            double amount = b.getAmount();

            String message = Messages.getInstance().getStringWithoutColoring("auction.bid", bidder, amount, "%item%",
                    getAmount(), getAuctionTime());

            MessageUtil.broadcastSpammy(auctioneer.getUniqueId(), formatMessage(message));
        }

        FileConfiguration data = ConfigManager.getConfig();

        if (!isSealed() && data.getBoolean("antisnipe.enabled") &&
                getAuctionTime() <= data.getInt("antisnipe.seconds-for-start")) {
            Auctions.getAuctionManager().getCurrentRunnable().antiSnipe();
        }
    }

    public boolean anyBids() {
        return !bidders.isEmpty();
    }

    public Bidder getLastBidder() {
        if (!bidders.isEmpty()) {
            List<Bidder> sorted = getBiddersHighestToLowest();
            return sorted.get(sorted.size() - 1);
        }

        return null;
    }

    public int getTimesBid(AuctionsPlayer ap) {
        int timesBid = 0;

        for (Bidder b : bidders) {
            if (b.getBidder() == ap) {
                timesBid = b.getTimesBid();
                break;
            }
        }

        return timesBid;
    }

    public int getConsecutiveBids(AuctionsPlayer ap) {
        Bidder bidder = getBidder(ap);

        if (bidder == null)
            return 0;
        else
            return bidder.getConsecutiveBids();
    }

    public List<Bidder> getBidders() {
        return bidders;
    }

    public List<Bidder> getLosingBidders() {
        ArrayList<Bidder> losing = new ArrayList<>(bidders);
        losing.remove(getLastBidder());

        return losing;
    }

    public Bidder getBidder(AuctionsPlayer ap) {
        for (Bidder bid : bidders) {
            if (bid.getBidder().equals(ap))
                return bid;
        }

        return null;
    }

    public List<Bidder> getBiddersHighestToLowest() {
        return bidders.stream().sorted(Comparator.comparingDouble(Bidder::getAmount)).
                collect(Collectors.toList());
    }

    public BaseComponent[] getAuctionInfo() {
        StringBuilder message;
        FileConfiguration data = ConfigManager.getConfig();

        if(data.getBoolean("general.custom-icon")){
            String itemCharacter ="";

            try{
               itemCharacter = UnicodeApi.getPlugin().instanceManager.getFilteredUnicodeCharacter(getItem());
            }catch (Exception e){
                System.out.println("Tried to get item " + getItem().getType().name() + "\n" +
                        "Exception: " + e );
            }

            message = new StringBuilder(Messages.getInstance().getStringWithoutColoring(
                    "auction.queue.info",
                    getAuctioneer().getOfflinePlayer().getName(),
                    getAmount(),
                    "%item%",
                    getStartingPrice(),
                    itemCharacter
                    ));
        }
        else{

            message = new StringBuilder(Messages.getInstance().getStringWithoutColoring(
                    "auction.queue.info",
                    getAuctioneer().getOfflinePlayer().getName(),
                    getAmount(),
                    "%item%",
                    getStartingPrice())
            );
        }

        return formatMessage(message.toString());
    }

    public BaseComponent[] getStartingMessage() {
        FileConfiguration data = ConfigManager.getConfig();
        StringBuilder message;
        if(data.getBoolean("general.custom-icon")){

            String itemCharacter = "";
            try{
                itemCharacter = UnicodeApi.getPlugin().instanceManager.getFilteredUnicodeCharacter(getItem());
            }catch (Exception e){
                System.out.println("Tried to get item " + getItem().getType().name() + "\n" +
                        "Exception: " + e );
            }


            message = new StringBuilder(Messages.getInstance().getStringWithoutColoring(
                    "auction.info",
                    getAuctioneer().getOfflinePlayer().getName(),
                    getAmount(),
                    "%item%",
                    getStartingPrice(),
                    getIncrement(),
                    getAuctionTime(),
                    itemCharacter
            ));
        }
        else{

            message = new StringBuilder(Messages.getInstance().getStringWithoutColoring(
                    "auction.info",
                    getAuctioneer().getOfflinePlayer().getName(),
                    getAmount(),
                    "%item%",
                    getStartingPrice(),
                    getIncrement(),
                    getAuctionTime())
            );
        }

        List<String> extra = new ArrayList<>();
        List<String> remove = new ArrayList<>();

        addAutoBuyBroadcast(extra);
        addHeadBroadcast(extra);
        addRepairBroadcast(extra);
        addSealedBroadcast(extra);
        addAverageBroadcast(extra);

        extra.stream().filter(s -> !s.startsWith("\n")).forEach(s -> {
            message.append(s);
            remove.add(s);
        });

        extra.removeAll(remove);

        extra.forEach(message::append);

        return formatMessage(message.toString());
    }

    private void addAutoBuyBroadcast(List<String> extra) {
        if (getAutoBuy() > 0) {
            extra.add(Messages.getString("auction.autobuy", getAutoBuy()));
        }
    }

    private void addHeadBroadcast(List<String> extra) {
        if (ConfigManager.getConfig().getBoolean("auctions.toggles.broadcast-head") &&
                getItem().getItemMeta() instanceof SkullMeta) {
            SkullMeta meta = (SkullMeta) getItem().getItemMeta();

            if (meta.hasOwner()) {
                extra.add(Messages.getString("auction.skull", meta.getOwningPlayer().getName()));
            }
        }
    }

    private void addRepairBroadcast(List<String> extra) {
        if (ConfigManager.getConfig().getBoolean("auctions.toggles.broadcast-repair")) {
            int xpToRepair = ReflectionUtil.getXPForRepair(getItem());
            if (xpToRepair == -1) {
                extra.add(Messages.getString("auction.repair.impossible"));
            } else if (xpToRepair > 0) {
                extra.add(Messages.getString("auction.repair.price", xpToRepair));
            }
        }
    }

    private void addAverageBroadcast(List<String> extra){
        if(Auctions.getAverageItemsList().stream().anyMatch(averageItem -> averageItem.getMaterial() == getItem().getType())){
            AverageItem avg =  Auctions.getAverageItemsList().stream().filter(averageItem -> averageItem.getMaterial() == getItem().getType()).findFirst().get();
            DecimalFormat f = new DecimalFormat("##.00");
            if(avg.getAveragePrice() != 0.0)
            {
                extra.add("\n" + Messages.getString("command.auction.average_display", avg.getMaterial().name().toLowerCase().replace("_"," "), f.format(avg.getAveragePrice())));
            }
        }
    }

    private void addSealedBroadcast(List<String> extra) {
        if (isSealed()) {
            extra.add(Messages.getString("auction.sealed"));
        }
    }

    public BaseComponent[] formatMessage(String message) {
        if (message.contains("%item%")) {
            String[] split = message.split("%item%");
            BaseComponent[] first = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', split[0])),
                    second = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
                            split[1]));

            BaseComponent lastComp = first[first.length - 1];

            // Component for the item
            BaseComponent itemComp;

            if (ConfigManager.getConfig().getBoolean("auctions.toggles.display-custom-name") && getItem().hasItemMeta()
                    && getItem().getItemMeta().hasDisplayName()) {
                String itemName = ChatColor.stripColor(getItem().getItemMeta().getDisplayName()); // Strip format

                if (ConfigManager.getConfig().getBoolean("auctions.toggles.quotes-around-name")) {
                    itemName = '"' + itemName + '"';
                }

                itemComp = new TextComponent(itemName);
            }
            else {
                String minecraftItemName = ReflectionUtil.getMinecraftName(getItem());
                itemComp = new TranslatableComponent(minecraftItemName);
            }

            itemComp.setColor(lastComp.getColor());
            itemComp.setBold(lastComp.isBold());
            itemComp.setItalic(lastComp.isItalic());
            itemComp.setStrikethrough(lastComp.isStrikethrough());
            itemComp.setUnderlined(lastComp.isUnderlined());

            //Todo check that this works

            Item seralisedItem = new Item(item.getType().getKey().getKey(), item.getAmount(), ItemTag.ofNbt(ReflectionUtil.nmsItemTag(getItem())));

            itemComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, seralisedItem));
            List<BaseComponent> list = new ArrayList<>();

            list.addAll(Arrays.asList(first));
            list.add(itemComp);
            list.addAll(Arrays.asList(second));

            BaseComponent[] combined = new BaseComponent[list.size()];
            combined = list.toArray(combined);

            return combined;
        } else {
            return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
