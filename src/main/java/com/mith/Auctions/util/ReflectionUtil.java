package com.mith.Auctions.util;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ConstantConditions")
public class ReflectionUtil {

    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";


    public static String getMinecraftName(ItemStack is) {
        try {
                Material material = is.getType();
                return (material.isBlock() ? "block" : "item") + ".minecraft." + material.toString().toLowerCase();
        } catch (Exception ex) {
            System.out.println("Error getting minecraft name for " + is.getType() + "\n" +
                    ex);
            return "";
        }
    }

    public static int getXPForRepair(ItemStack is) {
        try {
            Object nmsStack = asNMSCopy(is);

            //r = hasTag
            boolean hasTag = (boolean) nmsStack.getClass().getMethod("t").invoke(nmsStack);

            if (!hasTag)
                return 0;

            //s = getTag
            Object tag = nmsStack.getClass().getMethod("u").invoke(nmsStack);

            //q = hasKey
            boolean hasKey = (boolean) tag.getClass().getMethod("q", String.class).invoke(tag, "RepairCost");

            if (!hasKey)
                return 0;

            //h = getInt
            int cost = (int) tag.getClass().getMethod("h", String.class).invoke(tag, "RepairCost");
            boolean repairable = cost <= 40;

            if (repairable)
                return cost;
            else
                return -1;
        } catch (Exception ex) {
            System.out.println(
                    "Error getting xp needed for repair for " + is.getType().toString() + "\n" +
                            ex);
            return 0;
        }
    }

    public static String nmsItemTag(ItemStack item) {
        try {
            Object nmsStack =  asNMSCopy(item);

            boolean hasTag = (boolean) nmsStack.getClass().getMethod("t").invoke(nmsStack);

            if (!hasTag)
                return "";

            Object tag = nmsStack.getClass().getMethod("u").invoke(nmsStack);

            return tag.toString();
        } catch (Exception ex) {
            System.out.println(
                    "Error getting nmsItemTag for " + item.getType() + "\n" + "This is like due to NMS mappings for this particular version" +
                            ex);
            return "";
        }
    }

    private static Object asNMSCopy(ItemStack is) {
        try {
            return getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class).invoke(null, is);
        } catch (Exception ex) {
            System.out.println(
                    "Error getting item as NMS copy. Item: " + is.getType().toString() + "\n" +
                            ex);
            return null;
        }
    }

    public static Class<?> getOBCClass(String obcClassString) {
        String name = "org.bukkit.craftbukkit." + VERSION + obcClassString;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getCraftItemStackClass() {
        return getOBCClass("inventory.CraftItemStack");
    }

}