package net.aruneko.excavatortools.tools

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

/** Compare the held tool while allowing the original block's durability cost. */
class MiningToolSnapshot(item: ItemStack) {
    private val original = withoutDamage(item)

    fun matches(item: ItemStack): Boolean = original.isSimilar(withoutDamage(item))

    private fun withoutDamage(item: ItemStack): ItemStack = item.clone().also { copy ->
        val meta = copy.itemMeta
        if (meta is Damageable) {
            meta.damage = 0
            copy.itemMeta = meta
        }
    }
}
