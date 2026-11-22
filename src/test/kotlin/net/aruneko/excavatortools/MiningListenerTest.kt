package net.aruneko.excavatortools

import net.aruneko.excavatortools.listeners.MiningFaceListener
import net.aruneko.excavatortools.listeners.MiningListener
import net.aruneko.excavatortools.tools.MiningToolSnapshot
import net.aruneko.excavatortools.tools.ToolIdentity
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import java.lang.reflect.Proxy
import java.util.UUID
import kotlin.test.*

class MiningListenerTest {
    private val key = requireNotNull(NamespacedKey.fromString("excavatortools:tool_type"))
    private val queued = mutableListOf<Runnable>()
    private var online = true
    private var dead = false
    private var sneaking = false
    private var slot = 0
    private var worldId = UUID.randomUUID()
    private var gameMode = GameMode.SURVIVAL
    private var held: ItemStack = TestTool()
    private var centerType = Material.STONE
    private var breaks = 0
    private val playerId = UUID.randomUUID()
    private val world = stub<World> { name, _ ->
        when (name) {
            "getUID" -> worldId
            else -> error("Unexpected World.$name")
        }
    }
    private val inventory = stub<PlayerInventory> { name, _ ->
        when (name) {
            "getItemInMainHand" -> held
            "getHeldItemSlot" -> slot
            else -> error("Unexpected PlayerInventory.$name")
        }
    }
    private val player = stub<Player> { name, _ ->
        when (name) {
            "getUniqueId" -> playerId
            "getInventory" -> inventory
            "getWorld" -> world
            "getGameMode" -> gameMode
            "isOnline" -> online
            "isDead" -> dead
            "isSneaking" -> sneaking
            else -> error("Unexpected Player.$name")
        }
    }
    private val center = block()
    private val faces = MiningFaceListener()
    private val identity = ToolIdentity(key) { tag() }
    private val listener = MiningListener(faces, identity, { queued.add(it) }, { tag() })

    private fun tag(): Tag<Material> = stub { name, args ->
        when (name) {
            "isTagged" -> args[0] == Material.IRON_PICKAXE || args[0] == Material.STONE
            else -> error("Unexpected Tag.$name")
        }
    }

    private fun block(x: Int = 0, y: Int = 64, z: Int = 0, central: Boolean = true): Block = stub { name, args ->
        when (name) {
            "getWorld" -> world
            "getX" -> x
            "getY" -> y
            "getZ" -> z
            "getType" -> if (central) centerType else Material.STONE
            "getRelative" -> block(x + args[0] as Int, y + args[1] as Int, z + args[2] as Int, false)
            "breakNaturally" -> { breaks++; true }
            else -> error("Unexpected Block.$name")
        }
    }

    private fun begin(): BlockBreakEvent {
        faces.onInteract(PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, null, center, BlockFace.NORTH, EquipmentSlot.HAND))
        return BlockBreakEvent(center, player).also { listener.onBlockBreak(it) }
    }

    private fun finish() {
        centerType = Material.AIR
        queued.forEach { it.run() }
    }

    @Test
    fun `extra mining waits until next tick and permits original durability cost`() {
        val event = begin()
        assertEquals(0, breaks)
        assertEquals(1, queued.size)
        faces.onBreak(event)
        held = TestTool(damage = 1)
        finish()
        assertEquals(8, breaks)
    }

    @Test
    fun `later cancellation prevents all extra mining`() {
        val event = begin()
        event.isCancelled = true
        finish()
        assertEquals(0, breaks)
    }

    @Test
    fun `already cancelled events do not queue mining`() {
        faces.onInteract(PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, null, center, BlockFace.NORTH, EquipmentSlot.HAND))
        val event = BlockBreakEvent(center, player).apply { isCancelled = true }
        // The event dispatcher normally enforces ignoreCancelled; verify the queued task too.
        listener.onBlockBreak(event)
        finish()
        assertEquals(0, breaks)
    }

    @Test
    fun `logout death world change game mode change and sneaking stop queued mining`() {
        begin()
        online = false
        finish()
        online = true
        dead = true
        finish()
        dead = false
        val oldWorld = worldId
        worldId = UUID.randomUUID()
        finish()
        worldId = oldWorld
        gameMode = GameMode.CREATIVE
        finish()
        gameMode = GameMode.SURVIVAL
        sneaking = true
        finish()
        assertEquals(0, breaks)
    }

    @Test
    fun `unchanged original block prevents extra mining`() {
        begin()
        queued.single().run()
        assertEquals(0, breaks)
    }

    @Test
    fun `changing slot replacing tool or breaking tool stops queued mining`() {
        begin()
        slot = 1
        finish()
        slot = 0
        held = TestTool(label = "different tool")
        finish()
        held = TestTool(Material.AIR)
        finish()
        assertEquals(0, breaks)
    }

    @Test
    fun `tool comparison ignores damage without modifying actual tools`() {
        val original = TestTool(damage = 10)
        val current = TestTool(damage = 11)
        val snapshot = MiningToolSnapshot(original)
        assertTrue(snapshot.matches(current))
        assertFalse(snapshot.matches(TestTool(label = "different")))
        assertFalse(snapshot.matches(TestTool(Material.AIR)))
        assertEquals(10, (original.itemMeta as Damageable).damage)
        assertEquals(11, (current.itemMeta as Damageable).damage)
    }

    private class TestTool(
        material: Material = Material.IRON_PICKAXE,
        damage: Int = 0,
        private val label: String = "Hammer",
    ) : ItemStack(material) {
        private var metadata: ItemMeta? = if (material == Material.AIR) null else meta(damage, label)
        override fun getItemMeta(): ItemMeta? = metadata?.clone()
        override fun setItemMeta(meta: ItemMeta?): Boolean { metadata = meta?.clone(); return true }
        public override fun clone(): ItemStack = TestTool(type, (metadata as? Damageable)?.damage ?: 0, label)
        override fun isSimilar(other: ItemStack?): Boolean =
            other != null && type == other.type && itemMeta?.serialize() == other.itemMeta?.serialize()

        companion object {
            private fun meta(initialDamage: Int, label: String): ItemMeta {
                var damage = initialDamage
                val data = stub<PersistentDataContainer> { name, args ->
                    when (name) {
                        "has" -> (args[0] as NamespacedKey).toString() == "excavatortools:tool_type"
                        "get" -> "hammer"
                        else -> error("Unexpected PersistentDataContainer.$name")
                    }
                }
                return stub<Damageable> { name, args ->
                    when (name) {
                        "getDamage" -> damage
                        "setDamage" -> { damage = args[0] as Int; null }
                        "getPersistentDataContainer" -> data
                        "clone" -> meta(damage, label)
                        "serialize" -> mapOf("damage" to damage, "label" to label)
                        else -> error("Unexpected ItemMeta.$name")
                    }
                }
            }
        }
    }

    companion object {
        private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
            Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
                call(method.name, args ?: emptyArray())
            } as T
    }
}
