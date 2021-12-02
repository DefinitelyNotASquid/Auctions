package com.mith.Auctions.util;
import com.mith.Auctions.object.Auction;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemUtil {

    public static  List<Material> availableMaterials = new ArrayList<Material>(Arrays.asList(
            Material.DIAMOND_AXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_HOE,
            Material.DIAMOND_PICKAXE,
            Material.WOODEN_AXE,
            Material.WOODEN_SHOVEL,
            Material.WOODEN_SWORD,
            Material.WOODEN_HOE,
            Material.WOODEN_PICKAXE,
            Material.IRON_AXE,
            Material.IRON_SHOVEL,
            Material.IRON_SWORD,
            Material.IRON_HOE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_AXE,
            Material.GOLDEN_SHOVEL,
            Material.GOLDEN_SWORD,
            Material.GOLDEN_HOE,
            Material.GOLDEN_PICKAXE,
            Material.STONE_AXE,
            Material.STONE_SHOVEL,
            Material.STONE_SWORD,
            Material.STONE_HOE,
            Material.STONE_PICKAXE
    ));

    public static void confiscate(ItemStack itemStack, Player p, int amount){

        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (p.getInventory().getItem(i) == null)
                continue;

            if(!p.getInventory().getItem(i).getItemMeta().hasDisplayName()){
                continue;
            }

            ItemStack is = p.getInventory().getItem(i);

            if (!itemStack.isSimilar(is))
                continue;

            if (is.getAmount() > amount) {
                is.setAmount(is.getAmount() - amount);
                p.getInventory().setItem(i, is);

                break;
            }

            amount -= is.getAmount();
            p.getInventory().setItem(i, null);

            if (amount == 0)
                break;
        }

    }

    public static void removeItemsFromInv(Auction auc, Player p) {
        int amount = auc.getAmount();
        ItemStack auctionItem = auc.getItem();

        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (p.getInventory().getItem(i) == null)
                continue;

            ItemStack is = p.getInventory().getItem(i);

            if (!auctionItem.isSimilar(is))
                continue;

            if (is.getAmount() > amount) {
                is.setAmount(is.getAmount() - amount);
                p.getInventory().setItem(i, is);

                break;
            }

            amount -= is.getAmount();
            p.getInventory().setItem(i, null);

            if (amount == 0)
                break;
        }
    }

    static Material getMaterial(String type) {
        return Material.getMaterial(type.toUpperCase());
    }

    /**
     * @return true if there is overflow, false if not
     */
    static boolean addItemToInventory(Player p, ItemStack is, int amount, boolean message) {
        List<ItemStack> items = new ArrayList<>();
        boolean doesHaveCosmeticPermission = p.hasPermission("cosmetics.skin");

        if(!doesHaveCosmeticPermission){
            if(availableMaterials.contains(is.getType())){
                if(is.hasItemMeta()){
                    if(is.getItemMeta().hasCustomModelData())
                    {
                        ItemMeta itemMeta = is.getItemMeta();
                        itemMeta.setCustomModelData(null);
                        is.setItemMeta(itemMeta);
                    }
                }
            }
        }

        int maxStackSize = is.getMaxStackSize();

        while (amount > maxStackSize) {
            ItemStack cloned = is.clone();
            cloned.setAmount(maxStackSize);

            items.add(cloned);
            amount -= maxStackSize;
        }

        if (amount != 0) {
            ItemStack cloned = is.clone();
            cloned.setAmount(amount);

            items.add(cloned);
        }

        ItemStack[] array = new ItemStack[items.size()];
        array = items.toArray(array);

        Map<Integer, ItemStack> leftover = p.getInventory().addItem(array);

        if (!leftover.isEmpty()) {
            leftover.values().forEach(item -> p.getWorld().dropItemNaturally(p.getLocation(), item));

            if (message) {
                MessageUtil.privateMessage(p, "reward.full_inventory");
            }
        }

        return !leftover.isEmpty();
    }

    /**
     * Taken from https://gist.github.com/graywolf336/8153678#file-bukkitserialization-java
     */
    public static String serialize(ItemStack... items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(items.length);

            // Save every element in the list
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * Taken from https://gist.github.com/graywolf336/8153678#file-bukkitserialization-java
     */
    public static ItemStack deserialize(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            // Read the serialized inventory
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items[0];
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
