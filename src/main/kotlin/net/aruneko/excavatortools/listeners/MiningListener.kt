package net.aruneko.excavatortools.listeners

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class MiningListener(private val miningFaces: MiningFaceListener) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.isSneaking) return

        val mainHandItem = player.inventory.itemInMainHand
        val toolType = ToolType.from(mainHandItem) ?: return
        val face = miningFaces.consumeFace(player.uniqueId, event.block) ?: return

        event.block.surroundingBlocks(face).filter {
            toolType.mineableTag.isTagged(it.type)
        }.forEach {
            it.breakNaturally(mainHandItem)
        }
    }
}
