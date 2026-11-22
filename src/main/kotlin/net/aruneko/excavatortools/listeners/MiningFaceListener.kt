package net.aruneko.excavatortools.listeners

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID

class MiningFaceListener : Listener {
    private data class Target(val world: UUID, val x: Int, val y: Int, val z: Int) {
        companion object {
            fun of(block: Block) = Target(block.world.uid, block.x, block.y, block.z)
        }
    }

    private data class MiningFace(val target: Target, val face: BlockFace)

    private val faces = mutableMapOf<UUID, MiningFace>()

    @EventHandler(priority = EventPriority.MONITOR)
    fun onInteract(event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != Action.LEFT_CLICK_BLOCK && event.action != Action.LEFT_CLICK_AIR) return

        faces.remove(event.player.uniqueId)
        val block = event.clickedBlock ?: return
        if (event.action != Action.LEFT_CLICK_BLOCK) return
        // Merely observe the interaction; BlockBreakEvent decides whether mining is allowed.
        faces[event.player.uniqueId] = MiningFace(Target.of(block), event.blockFace)
    }

    fun consumeFace(playerId: UUID, block: Block): BlockFace? {
        val miningFace = faces.remove(playerId) ?: return null
        return miningFace.face.takeIf { miningFace.target == Target.of(block) }
    }

    @EventHandler
    fun onAbort(event: BlockDamageAbortEvent) {
        val playerId = event.player.uniqueId
        if (faces[playerId]?.target == Target.of(event.block)) faces.remove(playerId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onBreak(event: BlockBreakEvent) {
        // Also discard records for cancelled breaks, sneaking, and ordinary tools.
        faces.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        faces.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onWorldChange(event: PlayerChangedWorldEvent) {
        faces.remove(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onTeleport(event: PlayerTeleportEvent) {
        faces.remove(event.player.uniqueId)
    }

    fun clear() = faces.clear()
}
