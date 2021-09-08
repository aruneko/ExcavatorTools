package net.aruneko.excavatortools.listeners

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.recipes.Hammer.Companion.isHammer
import org.bukkit.Server
import org.bukkit.Tag.MINEABLE_PICKAXE
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.Plugin

class HammerListener(private val plugin: Plugin, private val server: Server): Listener {
    @EventHandler
    fun onUseHammer(event: BlockBreakEvent) {
        val player = event.player
        val mainHandItem = player.inventory.itemInMainHand

        if (player.isSneaking) {
            return
        }

        if (!mainHandItem.isHammer()) {
            return
        }

        event.block.surroundingBlocks(player).filter {
            MINEABLE_PICKAXE.isTagged(it.type)
        }.forEach {
            it.breakNaturally(mainHandItem)
        }
    }
}
