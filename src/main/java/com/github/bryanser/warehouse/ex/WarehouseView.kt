package com.github.bryanser.warehouse.ex

import Br.API.CallBack
import com.github.bryanser.brapi.ItemBuilder
import com.github.bryanser.brapi.Utils
import com.github.bryanser.brapi.kview.KViewContext
import com.github.bryanser.brapi.kview.KViewHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object WarehouseView {
    class WarehouseContext(p: Player) : KViewContext("§8物资仓库") {
        val pd = Data.getCacheData(p)!!
        var page = 0
    }

    val view = KViewHandler.createKView("WarehouseUI", 6, ::WarehouseContext) {
        icon(53) {
            initDisplay(ItemBuilder.createItem(Material.SLIME_BALL) {
                name("§6点击前往下一页")
            })
            click {
                if (page < 100) {
                    page++
                }
            }
        }
        icon(45) {
            initDisplay(ItemBuilder.createItem(Material.SLIME_BALL) {
                name("§6点击前往上一页")
            })
            click {
                if(page > 0){
                    page--
                }
            }
        }
        for(index in 0 until 45){
            icon(index){
                initDisplay {
                    val tar = index + page * 45 + 1
                    val ow = pd.getWarehouse(tar)
                    if (ow.isPresent) {
                        val display = ow.get().displayItem
                        val im = display.itemMeta
                        val name = if (ow.get().hasDisplayName()) ow.get().displayName else tar.toString()
                        im.displayName = name
                        val lore = mutableListOf<String>()
                        lore.add(String.format("§7左键点击打开<%s§7>仓库", name))
                        lore.add("§7右键更改仓库名字")
                        lore.add("§7手持物品shift+右键更改仓库材质")
                        im.lore = lore
                        display.itemMeta = im
                        return@initDisplay display
                        // return ItemBuilder.getBuilder(Material.CHEST).name(String.format("§6点击打开<%d>号仓库", tar)).build();
                    }
                    val price = PriceManager.getPrice(tar)
                    if (price == null) {
                        return@initDisplay null
                    }
                    ItemBuilder.createItem(Material.STAINED_GLASS_PANE,durability =  3){
                        name("§b§l点击花费" + price.getWarehousePrice() + "南风币 解锁这个仓库")
                    }
                }
                click(ClickType.LEFT){
                    val tar = index + page * 45 + 1
                    val p = player
                    val ow = pd.getWarehouse(tar)
                    if (ow.isPresent) {
                        val w = ow.get()
                        w.prepareData(p)
                        return@click
                    } else {
                        val price = PriceManager.getPrice(tar) ?: return@click
                        val e = Utils.economy!!
                        if (!e.has(p, price.warehousePrice.toDouble())) {
                            p.sendMessage("§c你没有足够的金币来解锁这个仓库")
                            return@click
                        }
                        e.withdrawPlayer(p, price.warehousePrice.toDouble())
                        pd.unlockWarehouse(tar)
                    }
                }
                click(ClickType.RIGHT){
                    val tar = index + page * 45 + 1
                    val p = player
                    val ow = pd.getWarehouse(tar)
                    if (ow.isPresent) {
                        val w = ow.get()
                        Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                            p.closeInventory()
                            CallBack.SendSignRequest(p) { t, s ->
                                w.displayName = ChatColor.translateAlternateColorCodes('&', s)
                                t.sendMessage("§6改名成功")
                            }
                        }, 1)
                    }
                }
                click(ClickType.SHIFT_RIGHT){
                    val tar = index + page * 45 + 1
                    val p = player
                    val ow = pd.getWarehouse(tar)
                    if (ow.isPresent) {
                        if (Br.API.Utils.hasItemInMainHand(p)) {
                            ow.get().displayItem = p.itemInHand
                            p.sendMessage("§6设置完成")
                        } else {
                            p.sendMessage("§c你的手上毛也没有")
                        }
                    }
                }
            }
        }
    }
}