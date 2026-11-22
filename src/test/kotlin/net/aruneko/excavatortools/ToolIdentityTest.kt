package net.aruneko.excavatortools

import net.aruneko.excavatortools.tools.ToolIdentity
import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ToolIdentityTest {
    private val key = requireNotNull(NamespacedKey.fromString("excavatortools:tool_type"))
    // Tag membership is supplied by the server in production.
    private val taggedMaterials = mapOf(
        ToolType.HAMMER to setOf(Material.WOODEN_PICKAXE, Material.IRON_PICKAXE, Material.DIAMOND_PICKAXE),
        ToolType.EXCAVATOR to setOf(Material.WOODEN_SHOVEL, Material.IRON_SHOVEL, Material.DIAMOND_SHOVEL),
    )
    private val identity = ToolIdentity(key) { type ->
        stub<Tag<Material>> { name, args ->
            when (name) {
                "isTagged" -> args[0] in taggedMaterials.getValue(type)
                else -> error("Unexpected Tag.$name")
            }
        }
    }

    @Test
    fun `marked tools are recognized for materials included in their tag`() {
        for (type in ToolType.entries) {
            for (material in taggedMaterials.getValue(type)) {
                val meta = metadata()
                identity.mark(meta, type)
                assertEquals(type, identity.identify(item(material, meta)))
            }
        }
    }

    @Test
    fun `legacy lore alone does not identify a tool`() {
        assertNull(identity.identify(item(Material.IRON_PICKAXE, metadata(lore = listOf("HAMMER!")))))
        assertNull(identity.identify(item(Material.IRON_SHOVEL, metadata(lore = listOf("EXCAVATOR!")))))
    }

    @Test
    fun `PDC identity works independently of lore`() {
        val meta = metadata(lore = listOf("renamed description"))
        identity.mark(meta, ToolType.HAMMER)
        assertEquals(ToolType.HAMMER, identity.identify(item(Material.DIAMOND_PICKAXE, meta)))
    }

    @Test
    fun `tool identity must match the material`() {
        for ((type, material) in listOf(
            ToolType.HAMMER to Material.IRON_SHOVEL,
            ToolType.EXCAVATOR to Material.IRON_PICKAXE,
            ToolType.HAMMER to Material.STICK,
        )) {
            val meta = metadata()
            identity.mark(meta, type)
            assertNull(identity.identify(item(material, meta)))
        }
    }

    @Test
    fun `unknown values wrong data types and other namespaces are ignored`() {
        val meta = metadata()
        val data = meta.persistentDataContainer
        data.set(key, PersistentDataType.STRING, "unknown")
        assertNull(identity.identify(item(Material.IRON_PICKAXE, meta)))
        data.set(key, PersistentDataType.INTEGER, 1)
        assertNull(identity.identify(item(Material.IRON_PICKAXE, meta)))
        data.remove(key)
        data.set(requireNotNull(NamespacedKey.fromString("other:tool_type")), PersistentDataType.STRING, "hammer")
        assertNull(identity.identify(item(Material.IRON_PICKAXE, meta)))
    }

    @Test
    fun `items without metadata are ordinary items`() {
        assertNull(identity.identify(item(Material.AIR, null)))
    }

    private fun item(material: Material, meta: ItemMeta?): ItemStack = object : ItemStack(material) {
        override fun getItemMeta(): ItemMeta? = meta
    }

    private fun metadata(lore: List<String>? = null): ItemMeta {
        val values = mutableMapOf<NamespacedKey, Pair<PersistentDataType<*, *>, Any>>()
        val data = stub<PersistentDataContainer> { name, args ->
            val key = args[0] as NamespacedKey
            when (name) {
                "set" -> { values[key] = (args[1] as PersistentDataType<*, *>) to requireNotNull(args[2]); null }
                "has" -> values[key]?.first == args[1]
                "get" -> values[key]?.second
                "remove" -> { values.remove(key); null }
                else -> error("Unexpected PersistentDataContainer.$name")
            }
        }
        return stub { name, _ ->
            when (name) {
                "getPersistentDataContainer" -> data
                "getLore" -> lore
                else -> error("Unexpected ItemMeta.$name")
            }
        }
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
