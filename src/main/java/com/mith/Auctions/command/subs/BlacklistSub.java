package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.object.Auction;
import com.mith.Auctions.object.AverageItem;
import com.mith.Auctions.object.Permission;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.Optional;

public class BlacklistSub extends SubCommand {

    public BlacklistSub() {
        super("blacklist", Permission.COMMAND_BLACKLIST, true, "bl");
    }

    @Override
    public void run(CommandSender sender, String[] args) {

        if (args.length != 2) {
            sendPropMessage(sender, "command.auction.blacklist.invalid_params");
            return;
        }

        Player p = Bukkit.getPlayer(args[1]);

        if(p == null)
        {
            sendPropMessage(sender, "command.auction.blacklist.could_not_find_player");
            return;
        }

        if(Auctions.bannedPlayers.contains(p.getUniqueId()))
        {
            sendPropMessage(sender, "command.auction.blacklist.unblacklisted_player", p.getName());
            Auctions.bannedPlayers.remove(p.getUniqueId());
            return;
        }

        Auctions.bannedPlayers.add(p.getUniqueId());
        sendPropMessage(sender, "command.auction.blacklist.blacklisted_player", p.getName());
    }
}
