package com.mith.Auctions.command.subs;

import com.mith.Auctions.Auctions;
import com.mith.Auctions.object.AverageItem;
import com.mith.Auctions.object.Permission;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.Optional;

public class AverageSub extends SubCommand {

    public AverageSub() {
        super("average", Permission.COMMAND_AVERAGE, true, "avg");
    }

    @Override
    public void run(CommandSender sender, String[] args) {

        Player player = (Player)sender;
        if(Auctions.bannedPlayers.contains(player.getUniqueId()))
        {
            sendPropMessage(sender, "command.auction.blacklist.you_have_been_blacklisted");
            return;
        }

        if (args.length < 2) {

            ItemStack is = player.getInventory().getItemInMainHand();

            if(is.getType() == Material.AIR)
            {
                sendPropMessage(sender, "command.auction.no_item_in_main_hand");
                return;
            }
            SendAveragePice(sender, is.getType().name().toLowerCase());
            return;
        }

        if (args.length > 2) {
            sendPropMessage(sender, "command.auction.too_many_arguments_average");
            return;
        }
        SendAveragePice(sender, args[1].toLowerCase());
    }

    public void SendAveragePice(CommandSender sender, String materialName)
    {
        Optional<AverageItem> didFind = Auctions.averageItemsList.stream().filter(averageItem -> averageItem.getMaterial().name().toLowerCase().equals(materialName)).findFirst();

        if(!didFind.isPresent()){
            sendPropMessage(sender, "command.auction.did_not_find_average_item");
            return;
        }

        AverageItem avg = didFind.get();

        double average = avg.getAveragePrice();

        if(average == 0)
        {
            sendPropMessage(sender, "command.auction.there_is_no_average_price_for", avg.getMaterial().name().toLowerCase().replace("_"," "));
            return;
        }
        DecimalFormat f = new DecimalFormat("##.00");
        sendPropMessage(sender, "command.auction.average_display", avg.getMaterial().name().toLowerCase().replace("_"," "), f.format(average) );
    }
}
