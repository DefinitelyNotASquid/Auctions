package com.mith.Auctions.util;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("ConstantConditions")
public class ReflectionUtil {

    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";




    public static String getMinecraftName(ItemStack is) {
        try {
            Object nmsStack = asNMSCopy(is);

            //c = getItem
            Object item = nmsStack.getClass().getMethod("c").invoke(nmsStack);

            if (getItemBannerClass().isAssignableFrom(item.getClass())) {
                Object enumColor = item.getClass().getMethod("c", nmsStack.getClass()).invoke(item, nmsStack);
                String color = enumColor.getClass().getMethod("d").invoke(enumColor).toString();
                return "item.banner." + color + ".name";
            } else {
                //m = getName
                Object translatableComponent = item.getClass().getMethod("m", nmsStack.getClass()).invoke(item, nmsStack);
                //i = getkey
                String content = translatableComponent.getClass().getMethod("i").invoke(translatableComponent).toString();
                return content;
            }
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
            boolean hasTag = (boolean) nmsStack.getClass().getMethod("r").invoke(nmsStack);

            if (!hasTag)
                return 0;

            //s = getTag
            Object tag = nmsStack.getClass().getMethod("s").invoke(nmsStack);

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

            boolean hasTag = (boolean) nmsStack.getClass().getMethod("r").invoke(nmsStack);

            if (!hasTag)
                return "";

            Object tag = nmsStack.getClass().getMethod("s").invoke(nmsStack);

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

    public static Class<?> getNMSClass(String nmsClass, String nmsPackage) {
        try {
            return Class.forName(nmsPackage + "." + nmsClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getCraftItemStackClass() {
        return getOBCClass("inventory.CraftItemStack");
    }

    private static Class<?> getLocaleLanguageClass() {
        return getNMSClass("Language", "net.minecraft.locale");
    }

    private static Class<?> getNBTTagCompoundClass() {
        return getNMSClass("NBTTagCompound", "net.minecraft.nbt");
    }

    private static Class<?> getItemBannerClass() {
        return getNMSClass("ItemBanner", "net.minecraft.world.item");
    }

}