package io.github.tinypot.tweaks.mdr

import io.github.monun.kommand.kommand
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ModifyDropRatePlugin : JavaPlugin() {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val configFile = File(dataFolder, "config.json")
    private lateinit var config: PluginConfig

    override fun onEnable() {
        config = if (configFile.exists()) {
            json.decodeFromString(configFile.readText())
        } else {
            dataFolder.mkdirs()

            PluginConfig().also { config ->
                configFile.writeText(json.encodeToString(config))
            }
        }

        server.pluginManager.registerEvents(MDREventListener(this, config), this)
        kommand {
            MDRKommand(config)(this)
        }

        if (config.autoSaveInterval > 0) {
            server.scheduler.runTaskTimer(this, Runnable {
                configFile.writeText(json.encodeToString(config))
            }, config.autoSaveInterval, config.autoSaveInterval)
        }
    }

    override fun onDisable() {
        configFile.writeText(json.encodeToString(config))
    }
}

@Serializable
data class PluginConfig(
    val autoSaveInterval: Long = 3 * 60 * 20, // 3 min default
    var global: SectionConfig = SectionConfig(),
    val players: MutableMap<String, SectionConfig> = hashMapOf()
)

@Serializable
data class SectionConfig(
    var enabled : Boolean = false,
    var block: Int = 1,
    var entity: Int = 1
) {
    override fun toString(): String {
        return "{ enabled: $enabled, block: $block, entity: $entity }"
    }
}