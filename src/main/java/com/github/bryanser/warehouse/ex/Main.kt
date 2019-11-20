/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */

package com.github.bryanser.warehouse.ex

import Br.API.GUI.Ex.UIManager
import Br.API.Utils
import com.github.bryanser.brapi.kview.KViewHandler
import com.github.bryanser.inventorycore.DatabaseHandler
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-10-21
 */
class Main : JavaPlugin() {

    override fun onEnable() {
        Plugin = this
        ConfigurationSerialization.registerClass(PriceManager.Price::class.java)
        if (!this.dataFolder.exists()) {
            this.saveDefaultConfig()
        }
        val config = this.config
        DatabaseHandler.connect(config.getString("SQL.IP"), config.getInt("SQL.Port"), config.getString("SQL.Database"), config.getString("SQL.User"), config.getString("SQL.Password"))
        Data.connect(config.getString("SQL.IP"), config.getInt("SQL.Port"), config.getString("SQL.Database"), config.getString("SQL.User"), config.getString("SQL.Password"))
        PriceManager.init(config.getInt("Setting.MaxWarehouse"))
        UIManager.RegisterUI(InvUI())
        Bukkit.getPluginManager().registerEvents(Data, this)
    }

    override fun onDisable() {
        for (p in Utils.getOnlinePlayers()) {
            Data.update(p.name, true)
        }
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<String>?): Boolean {
        if (sender is Player) {
            val pd = Data.getCacheData(sender)
            if (pd == null) {
                sender.sendMessage("§c数据还未准备好 请稍后重试")
                return true
            }
            KViewHandler.openUI(sender, WarehouseView.view)
            return true
        }
        return super.onCommand(sender, command, label, args)
    }

    companion object {

        lateinit var Plugin: Main
    }


}
