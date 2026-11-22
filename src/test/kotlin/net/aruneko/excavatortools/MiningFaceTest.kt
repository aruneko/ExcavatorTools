package net.aruneko.excavatortools

import net.aruneko.excavatortools.extensions.surroundingBlocks
import net.aruneko.excavatortools.listeners.MiningFaceListener
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageAbortEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.*

class MiningFaceTest {
    private val worldId = UUID.randomUUID()
    private val playerId = UUID.randomUUID()
    private val player = stub<Player> { name, _ ->
        when (name) {
            "getUniqueId" -> playerId
            else -> error("Unexpected Player.$name")
        }
    }
    private val world: World = stub { name, args ->
        when (name) {
            "getUID" -> worldId
            "getBlockAt" -> block(args[0] as Int, args[1] as Int, args[2] as Int)
            else -> error("Unexpected World.$name")
        }
    }

    private fun block(x: Int = 10, y: Int = 64, z: Int = 20, blockWorld: World = world): Block = stub { name, args ->
        when (name) {
            "getRelative" -> block(x + (args[0] as Int), y + (args[1] as Int), z + (args[2] as Int), blockWorld)
            "getWorld" -> blockWorld
            "getX" -> x
            "getY" -> y
            "getZ" -> z
            else -> error("Unexpected Block.$name")
        }
    }

    private fun click(listener: MiningFaceListener, block: Block = block(), face: BlockFace = BlockFace.NORTH) {
        listener.onInteract(PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, null, block, face, EquipmentSlot.HAND))
    }

    @Test
    fun `all six faces produce eight unique neighbors in the clicked plane`() {
        for (face in listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            val actual = block().surroundingBlocks(face).map { Triple(it.x - 10, it.y - 64, it.z - 20) }.toSet()
            val expected = buildSet {
                for (a in -1..1) for (b in -1..1) {
                    if (a == 0 && b == 0) continue
                    add(when (face) {
                        BlockFace.UP, BlockFace.DOWN -> Triple(a, 0, b)
                        BlockFace.NORTH, BlockFace.SOUTH -> Triple(a, b, 0)
                        else -> Triple(0, a, b)
                    })
                }
            }
            assertEquals(expected, actual, face.name)
        }
        assertTrue(block().surroundingBlocks(BlockFace.SELF).isEmpty())
    }

    @Test
    fun `face is consumed once and requires the same target`() {
        val listener = MiningFaceListener()
        click(listener)
        assertEquals(BlockFace.NORTH, listener.consumeFace(playerId, block()))
        assertNull(listener.consumeFace(playerId, block()))
        click(listener)
        assertNull(listener.consumeFace(playerId, block(x = 11)))
        assertNull(listener.consumeFace(playerId, block()))
    }

    @Test
    fun `same coordinates in another world do not match`() {
        val listener = MiningFaceListener()
        val otherWorldId = UUID.randomUUID()
        val otherWorld = stub<World> { name, _ ->
            when (name) {
                "getUID" -> otherWorldId
                else -> error("Unexpected World.$name")
            }
        }
        click(listener)
        assertNull(listener.consumeFace(playerId, block(blockWorld = otherWorld)))
    }

    @Test
    fun `offhand and right clicks preserve record but left click air clears it`() {
        val listener = MiningFaceListener()
        click(listener)
        listener.onInteract(PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, null, block(), BlockFace.UP, EquipmentSlot.OFF_HAND))
        listener.onInteract(PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, block(), BlockFace.UP, EquipmentSlot.HAND))
        assertEquals(BlockFace.NORTH, listener.consumeFace(playerId, block()))
        click(listener)
        listener.onInteract(PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, null, null, BlockFace.SELF, EquipmentSlot.HAND))
        assertNull(listener.consumeFace(playerId, block()))
    }

    @Test
    fun `new click replaces face and old abort does not erase it`() {
        val listener = MiningFaceListener()
        click(listener)
        click(listener, block(x = 11), BlockFace.UP)
        listener.onAbort(BlockDamageAbortEvent(player, block(), ItemStack(Material.AIR)))
        assertEquals(BlockFace.UP, listener.consumeFace(playerId, block(x = 11)))
    }

    @Test
    fun `matching abort cancelled break and shutdown discard records`() {
        val listener = MiningFaceListener()
        click(listener)
        listener.onAbort(BlockDamageAbortEvent(player, block(), ItemStack(Material.AIR)))
        assertNull(listener.consumeFace(playerId, block()))
        click(listener)
        listener.onBreak(BlockBreakEvent(block(), player).apply { isCancelled = true })
        assertNull(listener.consumeFace(playerId, block()))
        click(listener)
        listener.clear()
        assertNull(listener.consumeFace(playerId, block()))
    }

    @Test
    fun `extra break on another block does not erase the next clicked face`() {
        val listener = MiningFaceListener()
        click(listener, block(x = 11), BlockFace.UP)
        listener.onBreak(BlockBreakEvent(block(), player))
        assertEquals(BlockFace.UP, listener.consumeFace(playerId, block(x = 11)))
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
