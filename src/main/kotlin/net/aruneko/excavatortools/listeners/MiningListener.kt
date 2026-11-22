package net.aruneko.excavatortools.listeners

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.tools.ToolIdentity
import net.aruneko.excavatortools.tools.MiningToolSnapshot
import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import java.util.UUID

class MiningListener(
    private val miningFaces: MiningFaceListener,
    private val toolIdentity: ToolIdentity,
    private val runNextTick: (Runnable) -> Unit,
    private val mineableTags: (ToolType) -> Tag<Material> = { it.mineableTag },
) : Listener {
    private data class ExtraBreak(val block: Block, val suppressDrops: Boolean) {
        fun matches(other: Block): Boolean =
            block.world.uid == other.world.uid && block.x == other.x && block.y == other.y && block.z == other.z
    }

    private val extraBreaks = mutableMapOf<UUID, ExtraBreak>()

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        if (player.uniqueId in extraBreaks) return
        if (player.isSneaking) return

        val mainHandItem = player.inventory.itemInMainHand
        val toolType = toolIdentity.identify(mainHandItem) ?: return
        val face = miningFaces.consumeFace(player.uniqueId, event.block) ?: return
        val worldId = player.world.uid
        val slot = player.inventory.heldItemSlot
        val tool = MiningToolSnapshot(mainHandItem)
        val gameMode = player.gameMode
        val originalMaterial = event.block.type

        runNextTick(Runnable {
            // Other listeners may cancel the original event after this handler returns.
            fun canContinue(): Boolean {
                if (event.isCancelled || !player.isOnline || player.isDead) return false
                if (player.world.uid != worldId || player.gameMode != gameMode || player.isSneaking) return false
                if (player.inventory.heldItemSlot != slot) return false
                val currentItem = player.inventory.itemInMainHand
                return toolIdentity.identify(currentItem) == toolType && tool.matches(currentItem)
            }

            if (!canContinue()) return@Runnable
            // Do not mine neighbors if the original block was never removed (or was restored).
            if (event.block.type == originalMaterial) return@Runnable

            val mineableTag = mineableTags(toolType)
            for (block in event.block.surroundingBlocks(face)) {
                if (!canContinue()) break
                if (!mineableTag.isTagged(block.type)) continue

                extraBreaks[player.uniqueId] = ExtraBreak(block, suppressDrops = !event.isDropItems)
                try {
                    // Bukkit fires a separate cancellable event and handles drops, XP and durability.
                    player.breakBlock(block)
                } finally {
                    extraBreaks.remove(player.uniqueId)
                }
            }
        })
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onExtraBlockBreak(event: BlockBreakEvent) {
        val extra = extraBreaks[event.player.uniqueId] ?: return
        if (extra.suppressDrops && extra.matches(event.block)) event.isDropItems = false
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onExtraBlockDrop(event: BlockDropItemEvent) {
        val extra = extraBreaks[event.player.uniqueId] ?: return
        // Also cover a later listener re-enabling drops on the extra BlockBreakEvent.
        if (extra.suppressDrops && extra.matches(event.block)) event.isCancelled = true
    }
}
