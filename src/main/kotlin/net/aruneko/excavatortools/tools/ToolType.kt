package net.aruneko.excavatortools.tools

import org.bukkit.Material
import org.bukkit.Tag

enum class ToolType(val id: String, val displayName: String) {
    HAMMER("hammer", "Hammer"),
    EXCAVATOR("excavator", "Excavator");

    val itemTag: Tag<Material>
        get() = when (this) {
            HAMMER -> Tag.ITEMS_PICKAXES
            EXCAVATOR -> Tag.ITEMS_SHOVELS
        }

    val mineableTag: Tag<Material>
        get() = when (this) {
            HAMMER -> Tag.MINEABLE_PICKAXE
            EXCAVATOR -> Tag.MINEABLE_SHOVEL
        }

    companion object {
        fun fromId(id: String): ToolType? = entries.firstOrNull { it.id == id }
    }
}
