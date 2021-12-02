package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.manager.AuctionsPlayerManager;
import com.mith.Auctions.object.AuctionsPlayer;
import com.mith.Auctions.object.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IgnorePlayerSub extends SubCommand {

    public IgnorePlayerSub() {
        super("ignoreplayer", Permission.COMMAND_IGNORE_PLAYER, true, "ignorep");
    }

    @Override
    public void run(CommandSender sender, String[] args) {

        Player player = (Player)sender;
        if(Auctions.bannedPlayers.contains(player.getUniqueId()))
        {
            sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
            return;
        }

        if (args.length == 1) {
            sendPropMessage(sender, "command.auction.ignoreplayer.help");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sendPropMessage(sender, "command.auction.ignoreplayer.not_found");
            return;
        }

        if (player.getUniqueId() == target.getUniqueId()) {
            sendPropMessage(player, "command.auction.ignoreplayer.cannot_ignore_self");
            return;
        }

        AuctionsPlayer ap = AuctionsPlayerManager.getInstance().getPlayer(player.getUniqueId());

        boolean alreadyIgnoring = ap.getIgnoringPlayers().contains(target.getUniqueId());

        if (alreadyIgnoring) {
            ap.getIgnoringPlayers().remove(target.getUniqueId());
            sendPropMessage(player, "command.auction.ignoreplayer.not_ignoring", target.getName());
        } else {
            ap.getIgnoringPlayers().add(target.getUniqueId());
            sendPropMessage(player, "command.auction.ignoreplayer.is_ignoring", target.getName());
        }

        AuctionsPlayerManager.getInstance().saveGson();
    }
}
