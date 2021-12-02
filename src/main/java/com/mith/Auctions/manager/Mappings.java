package com.mith.Auctions.manager;

import com.mith.Auctions.Auctions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class Mappings {

    private static Mappings instance = new Mappings();

    private final File FILE = new File("plugins/Auctions", "iconmap.yml");

    private FileConfiguration data;

    private Mappings() {
        createFile();
        loadConfiguration();
    }

    public static Mappings getInstance() {
        return instance;
    }

    public static FileConfiguration getConfig() {
        return instance.getData();
    }

    private void createFile() {
        if (!FILE.getParentFile().isDirectory()) {
            FILE.getParentFile().mkdir();
        }

        if (!FILE.exists()) {
            try {
                FILE.createNewFile();

                InputStream input = getClass().getClassLoader().getResourceAsStream("iconmap.yml");
                OutputStream output = new FileOutputStream(FILE);

                Auctions.copy(input, output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfiguration() {
        data = YamlConfiguration.loadConfiguration(FILE);
    }

    private FileConfiguration getData() {
        return data;
    }

    public void reloadConfiguration() {
        createFile();
        loadConfiguration();
    }

    public Object get(String path) {
        return data.get(path);
    }
}
