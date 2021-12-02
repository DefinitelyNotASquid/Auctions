package com.mith.Auctions.util;

import com.mith.Auctions.manager.ConfigManager;
import com.mith.Auctions.object.Auction;
import com.mith.Auctions.object.AuctionsPlayer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;
import java.util.stream.Collectors;

public class AuctionUtil {

    public static Auction parseAuction(AuctionsPlayer ap, String amount, String startingPrice, String increment,
                                       String autoBuy, String time, boolean sealed) {
        Player p = ap.getOnlinePlayer();

        if (blockedWorld(p)) {
            message(p, "command.auction.start.blocked-worlds");
            return null;
        }

        ItemStack item = p.getInventory().getItemInMainHand().clone();

        if (item.getType() == Material.AIR) {
            message(p, "command.auction.start.cannot_auction_air");
            return null;
        }

        if (blockedMaterial(item)) {
            message(p, "command.auction.start.blocked-materials");
            return null;
        }

        if (damagedItem(item)) {
            message(p, "command.auction.start.damaged_item");
            return null;
        }

        int finalAmount = parseAmount(amount, p, item);

        if (finalAmount == -1) {
            message(p, "command.auction.start.invalid-amt");
            return null;
        }

        if(blockedName(item)){
            message(p, "command.auction.start.blocked-name");
            if(getConfig().getBoolean("blocked-item-names.destroy-on-detect"))
            {
                message(p, "command.auction.start.blocked-confiscated");
                ItemUtil.confiscate(item, p, finalAmount);
            }
            return null;
        }

        if (!isPositiveDouble(startingPrice)) {
            message(p, "command.auction.start.invalid_start_price");
            return null;
        }

        double finalStartingPrice = parseNumberFromConfig(startingPrice, "starting-price");

        if (!isPositiveDouble(increment)) {
            message(p, "command.auction.start.invalid-inc");
            return null;
        }

        double maximumStartingPrice = getConfig().getDouble("auctions.maximum.price");

        if(finalStartingPrice > maximumStartingPrice){
            sendPropMessage(p, "command.auction.start.maximum_start_price", maximumStartingPrice);
            return null;
        }

        double finalIncrement = getValueBasedOnConfig(increment, "increment");

        if(finalStartingPrice < finalIncrement)
        {
            message(p, "command.auction.start.starting-amount-lower-than-increment");
            return null;
        }

        if (!isPos0Double(autoBuy)) {
            message(p, "command.auction.start.invalid-buyout");
            return null;
        }

        double finalAutoBuy = getValueBasedOnConfig(autoBuy, "autobuy");

        if (finalAutoBuy > 0 && finalAutoBuy < finalStartingPrice) {
            message(p, "command.auction.start.invalid-buyout");
            return null;
        }

        if (!isPositiveDouble(time)) {
            message(p, "command.auction.start.invalid-time");
            return null;
        }

        int finalTime = getValueBasedOnConfig(time, "auction-time").intValue();

        return new Auction(ap, item, finalAmount, finalTime, finalStartingPrice, finalIncrement, finalAutoBuy, sealed);
    }

    private static int parseAmount(String amount, Player p, ItemStack item) {
        int finalAmount = -1;

        int total = getTotalItems(p, item);

        if (isPositiveDouble(amount)) {
            finalAmount = Integer.parseInt(amount);

            if (finalAmount > total)
                return -1;
        } else if (amount.equalsIgnoreCase("hand")) {
            finalAmount = item.getAmount();
        } else if (amount.equalsIgnoreCase("all")) {
            finalAmount = total;
        }

        return finalAmount;
    }

    private static int getTotalItems(Player p, ItemStack item) {
        int amount = 0;

        for (ItemStack is : p.getInventory().getContents())
            if (is != null && is.isSimilar(item))
                amount += is.getAmount();

        return amount;
    }

    private static Double getValueBasedOnConfig(String number, String config) {
        double d = parseNumberFromConfig(number, config);

        if (!betweenLimits(d, config)) {
            d = getDefault(config);
        }

        return d;
    }

    private static boolean betweenLimits(double number, String config) {
        double configMin = getConfig().getDouble("auctions.minimum." + config), configMax =
                getConfig().getDouble("auctions.maximum." + config);

        if (configMin == -1 && configMax == -1)
            return number == getDefault(config);
        else if (configMax == 0)
            return number >= configMin;
        else
            return number >= configMin && number <= configMax;
    }

    public static Double parseNumberFromConfig(String number, String config) {
        double d = Double.parseDouble(number);

        if (!getConfig().getBoolean("auctions.toggles.decimal." + config)) {
            d = Math.floor(d);
        }

        return d;
    }

    private static boolean isPositiveDouble(String input) {
        try {
            double d = Double.parseDouble(input);
            return d > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isPos0Double(String input) {
        try {
            double d = Double.parseDouble(input);
            return d >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private static Double getDefault(String config) {
        return getConfig().getDouble("auctions.default." + config);
    }

    public static boolean blockedWorld(Player p) {
        List<String> blockedWorlds =
                getConfig().getStringList("auctions.blocked-worlds").stream().map(String::toLowerCase)
                        .collect(Collectors.toList());

        return blockedWorlds.contains(p.getWorld().getName().toLowerCase());
    }

    private static boolean blockedMaterial(ItemStack is) {
        List<Material> blockedMaterials =
                getConfig().getStringList("auctions.blocked-materials").stream().map(ItemUtil::getMaterial)
                        .collect(Collectors.toList());

        return blockedMaterials.contains(is.getType());
    }

    private static boolean blockedName(ItemStack is){

        if(is.getItemMeta().hasDisplayName()){
            List<String> blockedwords = getConfig().getStringList("blocked-item-names.swearlist");
            String name = ChatColor.translateAlternateColorCodes('&', is.getItemMeta().getDisplayName().toLowerCase());
            for (String word: blockedwords){
                if(name.contains(word)){
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean damagedItem(ItemStack is) {
        Damageable damageMeta = (Damageable) is.getItemMeta();
        return ConfigManager.getConfig().getBoolean("auctions.toggles.restrict-damaged") && damageMeta != null  && damageMeta.hasDamage();
    }

    private static FileConfiguration getConfig() {
        return ConfigManager.getConfig();
    }

    private static void message(Player p, String prop) {
        MessageUtil.privateMessage(p, prop);
    }

    static void sendPropMessage(CommandSender sender, String property, Object... args) {
        MessageUtil.privateMessage(sender, property, args);
    }

}
