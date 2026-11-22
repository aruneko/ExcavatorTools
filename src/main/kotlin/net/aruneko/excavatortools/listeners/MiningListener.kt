package net.aruneko.excavatortools.listeners

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.tools.ToolIdentity
import net.aruneko.excavatortools.tools.MiningToolSnapshot
import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

class MiningListener(
    private val miningFaces: MiningFaceListener,
    private val toolIdentity: ToolIdentity,
    private val runNextTick: (Runnable) -> Unit,
    private val mineableTags: (ToolType) -> Tag<Material> = { it.mineableTag },
) : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
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
            if (event.isCancelled || !player.isOnline || player.isDead) return@Runnable
            if (player.world.uid != worldId || player.gameMode != gameMode || player.isSneaking) return@Runnable
            if (player.inventory.heldItemSlot != slot) return@Runnable
            // Do not mine neighbors if the original block was never removed (or was restored).
            if (event.block.type == originalMaterial) return@Runnable

            val currentItem = player.inventory.itemInMainHand
            if (toolIdentity.identify(currentItem) != toolType || !tool.matches(currentItem)) return@Runnable

            event.block.surroundingBlocks(face).filter {
                mineableTags(toolType).isTagged(it.type)
            }.forEach {
                it.breakNaturally(currentItem)
            }
        })
    }
}
