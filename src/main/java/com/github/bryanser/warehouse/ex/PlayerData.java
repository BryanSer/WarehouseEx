/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package com.github.bryanser.warehouse.ex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-10-21
 */
public class PlayerData implements Serializable {
    private static final long serialVersionUID = 0x00B1CCED;

    private List<Warehouse> Warehouses = new ArrayList<>();
    private String Name;

    public PlayerData(Player p) {
        this.Name = p.getName();
    }

    public void unlockWarehouse(int index) {
        Warehouses.add(new Warehouse(index));
    }

    public String getName() {
        return Name;
    }
    
    public Optional<Warehouse> getWarehouse(int index){
        for (int i = 0; i < Warehouses.size(); i++) {
            Warehouse w = Warehouses.get(i);
            if(w.getIndex() == index){
                return Optional.of(w);
            }
        }
        return Optional.empty();
    }
    
}
