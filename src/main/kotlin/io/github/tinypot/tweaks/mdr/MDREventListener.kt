package io.github.tinypot.tweaks.mdr

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.plugin.Plugin

class MDREventListener(private val plugin: Plugin, private val config: PluginConfig) : Listener {
    // originally written by monun
    private val placedMetadata by lazy { FixedMetadataValue(plugin, null) }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        event.block.setMetadata("placed", placedMetadata)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val name = player.name

        val section = config.players[name] ?: config.global

        if (section.enabled) {
            val block = event.block

            if (block.hasMetadata("placed")) {
                block.removeMetadata("placed", plugin)
                return
            }

            event.isDropItems = false

            repeat(section.block) {
                block.getDrops(player.inventory.itemInMainHand, player).forEach { dropItem ->
                    player.world.dropItemNaturally(event.block.location, dropItem)
                }
            }
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val player = entity.killer ?: return
        val name = player.name

        val section = config.players[name] ?: config.global

        // TODO: enhance entity drops (loot)
        if (section.enabled && entity !is Player) {
            val drops = event.drops
            val multiplier = section.entity

            repeat(multiplier) {
                drops.forEach { item ->
                    entity.world.dropItemNaturally(entity.location, item)
                }
            }

            drops.clear()
            event.droppedExp *= multiplier
        }
    }
}