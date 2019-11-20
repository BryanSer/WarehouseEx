/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package com.github.bryanser.warehouse.ex;

import Br.API.GUI.Ex.BaseUI;
import Br.API.GUI.Ex.Item;
import Br.API.GUI.Ex.Snapshot;
import Br.API.GUI.Ex.SnapshotFactory;
import Br.API.ItemBuilder;
import Br.API.Utils;
import com.github.bryanser.inventorycore.InventoryData;
import java.util.HashMap;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-10-16
 */
public class InvUI extends BaseUI {

    private static PlayerPointsAPI PAPI;

    static {
        PAPI = PlayerPoints.getPlugin(PlayerPoints.class).getAPI();
    }

    public static final String LOCK = "§r§e§l§o§c§r";

    public static Map<String, Warehouse> Opening = new HashMap<>();

    private Item[] Contains;
    private SnapshotFactory<InvUI> Factory = SnapshotFactory.getDefaultSnapshotFactory(this, (p, data) -> {
        Warehouse get = Opening.get(p.getName());
        data.put("Warehouse", get);
        InventoryData id = get.getCache();
        if(id== null){
            throw new IllegalArgumentException("§c数据异常");
        }
        data.put("Inv", id);
    });

    public InvUI() {
        super.Rows = 6;
        super.Name = "IU";
        super.DisplayName = "§6扩展背包";
        super.AllowShift = true;

        Contains = new Item[54];
        for (int i = 0; i < 54; i++) {
            int line = i / 9 + 1;
            int slot = i;
            Contains[i] = Item
                    .getNewInstance((p) -> {
                        Snapshot snap = this.getSnapshot(p);
                        Warehouse w = (Warehouse) snap.getData("Warehouse");
                        if (w.getUnlocked() < line) {
                            PriceManager.Price price = PriceManager.getPrice(w.getIndex());
                            return ItemBuilder.getBuilder(Material.STAINED_GLASS_PANE).durability((short) 3)
                                    .name(LOCK + "点击解锁本行")
                                    .lore("§6shift+左键点击 使用" + price.getMoneyPrice(w.getUnlocked() + 1) + "南风币 来解锁下行",
                                            "§6shift+右键点击 使用" + price.getPointPrice(w.getUnlocked() + 1) + "点卷 来解锁下行")
                                    .build();
                        }
                        InventoryData inv = (InventoryData) snap.getData("Inv");
                        return inv.geteItem(slot);
                    })
                    .setButtonPutable((p) -> {
                        Warehouse w = (Warehouse) this.getSnapshot(p).getData("Warehouse");
                        return w.getUnlocked() >= line;
                    })
                    .setClick(ClickType.SHIFT_LEFT, (p) -> {
                        Snapshot snap = this.getSnapshot(p);
                        Warehouse w = (Warehouse) snap.getData("Warehouse");
                        if (w.getUnlocked() >= line) {
                            return;
                        }
                        Economy eco = Utils.getEconomy();
                        PriceManager.Price price = PriceManager.getPrice(w.getIndex());
                        if (!eco.has(p, price.getMoneyPrice(w.getUnlocked() + 1))) {
                            p.sendMessage("§c你没有足够的南风币来解锁下一行");
                        } else {
                            eco.withdrawPlayer(p, price.getMoneyPrice(w.getUnlocked() + 1));
                            Bukkit.getScheduler().runTaskLater(Main.Companion.getPlugin(), () -> {
                                w.unlock();
                                p.closeInventory();
                                p.sendMessage("§6§l解锁完成");
                            }, 1);
                        }
                    })
                    .setClick(ClickType.SHIFT_RIGHT, (p) -> {
                        Snapshot snap = this.getSnapshot(p);
                        Warehouse w = (Warehouse) snap.getData("Warehouse");
                        if (w.getUnlocked() >= line) {
                            return;
                        }
                        PriceManager.Price price = PriceManager.getPrice(w.getIndex());
                        if (PAPI.look(p.getName()) < price.getPointPrice(w.getUnlocked() + 1)) {
                            p.sendMessage("§c你没有足够的点卷来解锁下一行");
                        } else {
                            PAPI.take(p.getName(), price.getPointPrice(w.getUnlocked() + 1));
                            Bukkit.getScheduler().runTaskLater(Main.Companion.getPlugin(), () -> {
                                w.unlock();
                                p.closeInventory();
                                p.sendMessage("§6§l解锁完成");
                            }, 1);
                        }
                    })
                    .setUpdate(false)
                    .setUpdateIcon(false);
        }
    }

    @Override
    public void onClose(Player p, Snapshot s) {
        Inventory inv = s.getInventory();
        InventoryData data = (InventoryData) s.getData("Inv");
        for (int i = 0; i < super.Rows * 9; i++) {
            ItemStack is = inv.getItem(i);
            if (is != null && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().contains(LOCK)) {
                continue;
            }
            data.update(i, inv.getItem(i));
        }
        Warehouse w = (Warehouse) s.getData("Warehouse");
        w.releaseData();
    }

    @Override
    public Item getItem(Player p, int slot) {
        return Contains[slot];
    }

    @Override
    public SnapshotFactory<InvUI> getSnapshotFactory() {
        return this.Factory;
    }

}
