/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package com.github.bryanser.warehouse.ex;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-10-27
 */
public class PriceManager {

    public static int MaxWarehouse = 23;
    private static Map<Integer, Price> Prices = new HashMap<>();

    public static Price getPrice(int index) {
        return Prices.get(index);
    }

    public static void init(int maxw) {
        MaxWarehouse = maxw;
        if (!Main.Companion.getPlugin().getDataFolder().exists()) {
            Main.Companion.getPlugin().getDataFolder().mkdirs();
        }
        File f = new File(Main.Companion.getPlugin().getDataFolder(), "price.yml");
        boolean edit = false;
        if (!f.exists()) {
            try {
                f.createNewFile();
                edit = true;
            } catch (IOException ex) {
                Logger.getLogger(PriceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
        for (int i = 1; i < MaxWarehouse; i++) {
            if (config.contains("Warehouse-" + i)) {
                Prices.put(i, (Price) config.get("Warehouse-" + i));
            } else {
                Price p = new Price();
                Prices.put(i, p);
                edit = true;
                config.set("Warehouse-" + i, p);
            }
        }
        if (edit) {
            try {
                config.save(f);
            } catch (IOException ex) {
                Logger.getLogger(PriceManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static class Price implements ConfigurationSerializable {

        private int WarehousePrice = 10;

        private int[] Price_Money = new int[6];
        private int[] Pirce_Point = new int[6];

        public Price() {
            for (int i = 0; i < 6; i++) {
                Price_Money[i] = 100;
                Pirce_Point[i] = 100;
            }
        }

        public Price(Map<String, Object> args) {
            WarehousePrice = (int) args.get("WarehousePrice");
            for (int i = 0; i < 6; i++) {
                Price_Money[i] = -1;
                Pirce_Point[i] = -1;
                Price_Money[i] = (int) args.get(String.format("Money_%d", i + 1));
                Pirce_Point[i] = (int) args.get(String.format("Point_%d", i + 1));
            }
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("WarehousePrice", WarehousePrice);
            for (int i = 0; i < 6; i++) {
                map.put(String.format("Money_%d", i + 1), Price_Money[i]);
                map.put(String.format("Point_%d", i + 1), Pirce_Point[i]);
            }
            return map;
        }

        public int getMoneyPrice(int line) {
            return Price_Money[line - 1];
        }

        public int getPointPrice(int line) {
            return Pirce_Point[line - 1];
        }

        public int getWarehousePrice() {
            return WarehousePrice;
        }

    }

    private PriceManager() {
    }
}
