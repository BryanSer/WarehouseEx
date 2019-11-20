/*
 * 开发者:Bryan_lzh
 * QQ:390807154
 * 保留一切所有权
 * 若为Bukkit插件 请前往plugin.yml查看剩余协议
 */
package com.github.bryanser.warehouse.ex

import com.github.bryanser.inventorycore.Tools
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.HashMap
import java.util.logging.Level
import java.util.logging.Logger
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector

/**
 *
 * @author Bryan_lzh
 * @version 1.0
 * @since 2018-10-27
 */
@Suppress("SqlResolve")
object Data : Listener {
    operator fun HikariDataSource.unaryPlus(): Connection = this.connection
    operator fun HikariDataSource.minus(conn: Connection): Unit = this.evictConnection(conn)

    @EventHandler
    fun onJoin(evt: PlayerJoinEvent) {
        Bukkit.getScheduler().runTaskLater(Main.Plugin, { getCacheData(evt.player) }, 10)
    }

    @EventHandler
    fun onExit(evt: PlayerQuitEvent) {
        update(evt.player.name, true)
    }

    @EventHandler
    fun onShift(evt: PlayerToggleSneakEvent) {
        if (evt.isSneaking) {
            val v = evt.player.location.direction
            if (Math.abs(v.angle(Vector(0, 1, 0))) < Math.PI / 12) {
                Bukkit.dispatchCommand(evt.player, "WarehouseEx open")
            }
        }
    }

    lateinit var pool: HikariDataSource

    private val Cache = HashMap<String, PlayerData>()

    fun connect(host: String, port: Int, database: String, user: String, password: String) {
        val connect = String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&rewriteBatchedStatements=true",
                host, port, database, user, password)
        try {
            val config = HikariConfig()
            config.jdbcUrl = connect
            config.addDataSourceProperty("cachePrepStmts", "true")
            config.addDataSourceProperty("prepStmtCacheSize", "250")
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
            config.idleTimeout = 60000
            config.connectionTimeout = 60000
            config.validationTimeout = 3000
            config.maxLifetime = 60000
            pool = HikariDataSource(config)
            sql {

                val sta = this.createStatement()
                sta.execute("CREATE TABLE IF NOT EXISTS WarehouseEx(Player VARCHAR(40), Data BLOB, PRIMARY KEY(`Player`)) ENGINE = InnoDB DEFAULT CHARSET=utf8")

            }
        } catch (e: Exception) {
        }

    }

    fun getCacheData(p: Player): PlayerData? {
        var pd: PlayerData? = Cache[p.name]
        if (pd == null) {
            getData(p.name) {
                if (it == null) {
                    pd = PlayerData(p)
                    insertData(pd!!)
                }else{
                    pd = it
                }
                Cache[p.name] = pd!!
            }
            return pd
        }
        return pd
    }

    fun update(name: String, delete: Boolean) {
        val pd = Cache[name]
        if (pd != null) {
            Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
                sql {
                    val updateData = prepareStatement("UPDATE WarehouseEx SET Data = ? WHERE Player = ? LIMIT 1")
                    updateData.setObject(1, pd)
                    updateData.setString(2, name)
                    updateData.execute()
                }
                if (delete) {
                    Bukkit.getScheduler().runTask(Main.Plugin) {
                        Cache.remove(name)
                    }
                }
            }

        }
    }

    private fun insertData(pd: PlayerData) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            sql {
                val insertData = prepareStatement("INSERT INTO WarehouseEx VALUES (?, ?)")
                insertData.setString(1, pd.name)
                insertData.setObject(2, pd)
                insertData.execute()
            }
        }

    }

    private fun getData(name: String, get: (PlayerData?) -> Unit) {

        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            sql {
                val getData = prepareStatement("SELECT Data FROM WarehouseEx WHERE Player = ? LIMIT 1")
                getData.setString(1, name)
                val rs = getData.executeQuery()
                val pd: PlayerData?
                if (rs.next()) {
                    pd = Tools.getObject(rs, 1, PlayerData::class.java)
                } else {
                    pd = null
                }
                Bukkit.getScheduler().runTask(Main.Plugin) {
                    get(pd)
                }
            }
        }
    }

    inline fun sql(func: Connection.() -> Unit) {
        val conn = pool.connection
        try {
            conn.func()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            pool.evictConnection(conn)
        }
    }

}
