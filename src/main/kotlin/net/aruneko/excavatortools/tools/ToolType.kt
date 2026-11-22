package net.aruneko.excavatortools.tools

import net.aruneko.excavatortools.recipes.Excavator.Companion.isExcavator
import net.aruneko.excavatortools.recipes.Hammer.Companion.isHammer
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack

enum class ToolType {
    HAMMER,
    EXCAVATOR;

    val mineableTag: Tag<Material>
        get() = when (this) {
            HAMMER -> Tag.MINEABLE_PICKAXE
            EXCAVATOR -> Tag.MINEABLE_SHOVEL
        }

    private fun matches(item: ItemStack): Boolean = when (this) {
        HAMMER -> item.isHammer()
        EXCAVATOR -> item.isExcavator()
    }

    companion object {
        fun from(item: ItemStack): ToolType? = entries.firstOrNull { it.matches(item) }
    }
}
