/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package com.github.bryanser.warehouse.ex;

import Br.API.GUI.Ex.UIManager;
import com.github.bryanser.inventorycore.InvCore;
import com.github.bryanser.inventorycore.InventoryData;
import java.io.Serializable;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-10-27
 */
public class Warehouse implements Serializable {

    private static final long serialVersionUID = 0x00B132AED;

    private UUID UID;
    private int Unlocked = 1;
    private int Index;
    private Material mate;
    private short Dur = 0;
    private String DisplayName = "";

    private transient InventoryData Cache;
    private transient ItemStack DisplayItem;

    public Warehouse(int Index) {
        this.Index = Index;
        UID = InvCore.createNewInventory(6, null);
    }

    public boolean hasDisplayName() {
        return !this.DisplayName.isEmpty();
    }

    public ItemStack getDisplayItem() {
        if (this.DisplayItem == null) {
            if (this.mate != null) {
                this.DisplayItem = new ItemStack(mate, 1, Dur);
            }else {
                this.DisplayItem = new ItemStack(Material.CHEST);
            }
        }
        return this.DisplayItem.clone();
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public void setDisplayItem(ItemStack is) {
        this.mate = is.getType();
        this.Dur = is.getDurability();
        this.DisplayItem = null;
    }

    public UUID getUID() {
        return UID;
    }

    public int getUnlocked() {
        return Unlocked;
    }

    public int getIndex() {
        return Index;
    }

    public void unlock() {
        if (Unlocked < 6) {
            Unlocked++;
        }
    }

    public void prepareData(Player p) {
        InvCore.getInventory(UID, false, (id) -> {
            if(id == null){
                p.sendMessage("§c读取数据时发生错误 背包可能还在被占用中");
                return;
            }
            this.Cache = id;
            InvUI.Opening.put(p.getName(), this);
            UIManager.OpenUI(p, "IU");
        });
    }

    public void releaseData() {
        if (Cache != null) {
            InvCore.update(Cache, true, true);
            Cache = null;
        }
    }

    public void setDisplayName(String DisplayName) {
        this.DisplayName = DisplayName;
    }

    public InventoryData getCache() {
        return Cache;
    }

}
