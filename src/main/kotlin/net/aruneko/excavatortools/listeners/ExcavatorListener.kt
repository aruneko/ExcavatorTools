package net.aruneko.excavatortools.listeners

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.recipes.Excavator.Companion.isExcavator
import org.bukkit.Server
import org.bukkit.Tag.MINEABLE_SHOVEL
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.Plugin

class ExcavatorListener(private val plugin: Plugin, private val server: Server): Listener {
    @EventHandler
    fun onUseExcavator(event: BlockBreakEvent) {
        val player = event.player
        val mainHandItem = player.inventory.itemInMainHand

        if (player.isSneaking) {
            return
        }

        if (!mainHandItem.isExcavator()) {
            return
        }

        event.block.surroundingBlocks(player).filter {
            MINEABLE_SHOVEL.isTagged(it.type)
        }.forEach {
            it.breakNaturally(mainHandItem)
        }
    }
}
