package net.aruneko.excavatortools.tools

import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

class ToolIdentity(
    private val key: NamespacedKey,
    private val itemTags: (ToolType) -> Tag<Material> = { it.itemTag },
) {
    fun mark(meta: ItemMeta, toolType: ToolType) {
        meta.persistentDataContainer.set(key, PersistentDataType.STRING, toolType.id)
    }

    fun identify(item: ItemStack): ToolType? {
        val data = item.itemMeta?.persistentDataContainer ?: return null
        if (!data.has(key, PersistentDataType.STRING)) return null
        val id = data.get(key, PersistentDataType.STRING) ?: return null
        val toolType = ToolType.fromId(id) ?: return null
        return toolType.takeIf { itemTags(it).isTagged(item.type) }
    }
}
