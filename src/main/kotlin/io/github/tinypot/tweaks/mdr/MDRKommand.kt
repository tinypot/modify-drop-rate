package io.github.tinypot.tweaks.mdr

import io.github.monun.kommand.PluginKommand
import io.github.monun.kommand.StringType
import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.KommandNode
import kotlin.reflect.KMutableProperty1
import org.bukkit.entity.Player
import java.util.EnumSet

internal class MDRKommand(private val config: PluginConfig): (PluginKommand) -> Unit {
    override fun invoke(kommand: PluginKommand) {
        kommand.register("drop") {
            requires { isOp }

            then("global") {
                apply(true)
            }

            then("player") {
                then("players" to players()) {
                    apply(false)
                }

                then("clear") {
                    executes {
                        config.players.clear()

                        sender.sendMessage("player = ${config.players}")
                    }
                }
            }

            then("clear") {
                executes {
                    config.global = SectionConfig()
                    config.players.clear()

                    sender.sendMessage("global = ${config.global}")
                    sender.sendMessage("player = ${config.players}")
                }
            }
        }
    }

    private fun KommandNode.apply(isGlobal: Boolean = false) {
        then("get") {
            executes {
                if (isGlobal) {
                    sender.sendMessage("global = ${config.global}")
                } else {
                    val players: Collection<Player> by it

                    players.forEach { player ->
                        val name = player.name

                        sender.sendMessage("player.$name = ${config.players[name]}")
                    }
                }
            }
        }

        then("set") {
            then("enabled") {
                then("value" to bool()) {
                    executes {
                        val value: Boolean by it

                        if (isGlobal) {
                            config.global.enabled = value

                            sender.sendMessage("global.enabled = $value")
                        } else {
                            val players: Collection<Player> by it

                            players.forEach { player ->
                                val name = player.name
                                val section = config.players[name]?.takeIf {playerConfig -> playerConfig.enabled }
                                    ?: SectionConfig().also { sectionConfig ->
                                        sectionConfig.block = config.global.block
                                        sectionConfig.entity = config.global.entity

                                        config.players[name] = sectionConfig
                                    }

                                section.enabled = value

                                sender.sendMessage("player.$name.enabled = $value")
                            }
                        }
                    }
                }
            }

            then("multiplier") {
                val enumSet = EnumSet.allOf(DropType::class.java)
                then("type" to dynamic(StringType.SINGLE_WORD) { _, input ->
                    enumSet.find { it.name.lowercase() == input.lowercase() }
                }.apply {
                    suggests {
                        suggest(enumSet, { it.name.lowercase() })
                    }
                }) {
                    then("value" to int(0)) {
                        executes {
                            val type: DropType by it
                            val value: Int by it

                            if (isGlobal) {
                                type.properties.forEach { property ->
                                    property.set(config.global, value)

                                    sender.sendMessage("global.${property.name} = $value")
                                }
                            } else {
                                val players: Collection<Player> by it

                                players.forEach { player ->
                                    val name = player.name
                                    val section = config.players[name]?.takeIf { playerConfig -> playerConfig.enabled }
                                        ?: SectionConfig().also { sectionConfig ->
                                            sectionConfig.enabled = config.global.enabled

                                            config.players[name] = sectionConfig
                                        }

                                    type.properties.forEach { property ->
                                        property.set(section, value)

                                        sender.sendMessage("player.$name.${property.name} = $value")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        then("clear") {
            executes {
                if (isGlobal) {
                    config.global = SectionConfig()

                    sender.sendMessage("global = ${config.global}")
                } else {
                    val players: Collection<Player> by it

                    players.forEach { player ->
                        val name = player.name
                        config.players.remove(name)

                        sender.sendMessage("player.$name = null")
                    }
                }
            }
        }
    }
}

enum class DropType(val properties: Set<KMutableProperty1<SectionConfig, Int>>) {
    BLOCK(setOf(SectionConfig::block)),
    ENTITY(setOf(SectionConfig::entity)),
    ALL(setOf(SectionConfig::block, SectionConfig::entity))
}