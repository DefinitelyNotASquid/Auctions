package com.mith.Auctions;

import com.mith.Auctions.object.AverageItem;
import net.milkbowl.vault.economy.Economy;
import com.mith.Auctions.command.AuctionCommand;
import com.mith.Auctions.command.BidCommand;
import com.mith.Auctions.listener.JoinListener;
import com.mith.Auctions.manager.AuctionManager;
import org.bukkit.Material;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Auctions extends JavaPlugin {

    private static AuctionManager auctionManager;
    private static Economy econ;
    public static List<UUID> bannedPlayers = new ArrayList<>();
    public static List<AverageItem> averageItemsList = new ArrayList<>();
    public static List<AverageItem> getAverageItemsList() {return averageItemsList;}
    public static void setAverageItemsList(List<AverageItem> update){averageItemsList = update;}
    public static AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public static Economy getEcon() {
        return econ;
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault not detected! Is Vault installed along with a supported economy provider? " +
                    "Disabling plugin...");
            setEnabled(false);

            return;
        }

        registerListener();
        registerCommands();
        registerAuctionManger();
        loadAverages();
        loadBannedPlayers();
    }

    @Override
    public void onDisable() {
        getAuctionManager().disabling();
        saveAverages();
        saveBannedPlayers();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
            return false;

        econ = rsp.getProvider();

        return econ != null;
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
    }

    private void registerCommands() {
        getCommand("auctions").setExecutor(new AuctionCommand());
        getCommand("bid").setExecutor(new BidCommand());
    }

    private void registerAuctionManger() {
        auctionManager = new AuctionManager(this);
    }

    public static int copy(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024 * 4];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }

        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private void saveAverages(){
        try {
            FileWriter myWriter = new FileWriter("plugins/Auctions/itemAverages.txt");

            StringBuilder sb = new StringBuilder();
            for (AverageItem avg: averageItemsList) {
                sb.append(avg.getMaterial().name()+":"+avg.getAmount()+":"+ avg.getPrice()+"\n");
            }
            myWriter.write(sb.toString());
            myWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAverages() {
        File FILE = new File("plugins/Auctions", "itemAverages.txt");

        if (!FILE.getParentFile().isDirectory()) {
            FILE.getParentFile().mkdir();
        }

        if (!FILE.exists()) {
            try {
                FILE.createNewFile();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                for(Material x : Material.values())
                {
                    baos.write((x.toString()+":0.0:0.0\n").getBytes());
                }
                InputStream input = new ByteArrayInputStream(baos.toByteArray());
                OutputStream output = new FileOutputStream(FILE);

                Auctions.copy(input, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            InputStream listInput = new FileInputStream(FILE);
            BufferedReader bufferedReader = null;

                bufferedReader = new BufferedReader(new InputStreamReader(listInput, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) { // Ignored lines
                    continue;
                }
                String[] arrOfStr  = line.split(":");
                AverageItem tempAverage = new AverageItem(Double.parseDouble(arrOfStr[1]), Double.parseDouble(arrOfStr[2]), Material.getMaterial(arrOfStr[0]));
                averageItemsList.add(tempAverage);
            }
            bufferedReader.close();
            listInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBannedPlayers(){
        try {
            FileWriter myWriter = new FileWriter("plugins/Auctions/bannedPlayers.txt");
            StringBuilder sb = new StringBuilder();
            for (UUID bannedPlayer: bannedPlayers) {
                sb.append(bannedPlayer.toString()+"\n");
            }
            myWriter.write(sb.toString());
            myWriter.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBannedPlayers() {
        File FILE = new File("plugins/Auctions", "bannedPlayers.txt");

        if (!FILE.getParentFile().isDirectory()) {
            FILE.getParentFile().mkdir();
        }

        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            InputStream listInput = new FileInputStream(FILE);
            BufferedReader bufferedReader = null;

            bufferedReader = new BufferedReader(new InputStreamReader(listInput, "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) { // Ignored lines
                    continue;
                }
                bannedPlayers.add(UUID.fromString(line));
            }
            bufferedReader.close();
            listInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
