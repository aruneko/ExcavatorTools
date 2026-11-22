package net.aruneko.excavatortools.listeners

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.recipes.Excavator.Companion.isExcavator
import org.bukkit.Tag.MINEABLE_SHOVEL
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class ExcavatorListener(private val miningFaces: MiningFaceListener): Listener {
    @EventHandler(ignoreCancelled = true)
    fun onUseExcavator(event: BlockBreakEvent) {
        val player = event.player
        val mainHandItem = player.inventory.itemInMainHand

        if (player.isSneaking) {
            return
        }

        if (!mainHandItem.isExcavator()) {
            return
        }

        val face = miningFaces.consumeFace(player.uniqueId, event.block) ?: return

        event.block.surroundingBlocks(face).filter {
            MINEABLE_SHOVEL.isTagged(it.type)
        }.forEach {
            it.breakNaturally(mainHandItem)
        }
    }
}
