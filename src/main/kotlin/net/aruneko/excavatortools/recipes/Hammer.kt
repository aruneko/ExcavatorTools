package net.aruneko.excavatortools.recipes

import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class Hammer() {
    companion object {
        private val pickaxes = listOf(
            MaterialPair("Wooden", Material.WOODEN_PICKAXE, Material.OAK_WOOD),
            MaterialPair("Wooden", Material.WOODEN_PICKAXE, Material.ACACIA_WOOD),
            MaterialPair("Wooden", Material.WOODEN_PICKAXE, Material.BIRCH_WOOD),
            MaterialPair("Wooden", Material.WOODEN_PICKAXE, Material.DARK_OAK_WOOD),
            MaterialPair("Wooden", Material.WOODEN_PICKAXE, Material.JUNGLE_WOOD),
            MaterialPair("Wooden", Material.WOODEN_PICKAXE, Material.SPRUCE_WOOD),
            MaterialPair("Stone", Material.STONE_PICKAXE, Material.COBBLESTONE),
            MaterialPair("Stone", Material.STONE_PICKAXE, Material.BLACKSTONE),
            MaterialPair("Iron", Material.IRON_PICKAXE, Material.IRON_INGOT),
            MaterialPair("Golden", Material.GOLDEN_PICKAXE, Material.GOLD_INGOT),
            MaterialPair("Diamond", Material.DIAMOND_PICKAXE, Material.DIAMOND),
            MaterialPair("Netherite", Material.NETHERITE_PICKAXE, Material.NETHERITE_INGOT),
        )

        private fun ItemStack.isPickAxe(): Boolean {
            return pickaxes.map{ it.tool }.contains(this.type)
        }

        fun ItemStack.isHammer(): Boolean {
            return this.isPickAxe() && this.itemMeta?.lore?.contains("HAMMER!") ?: false
        }

        fun addRecipes(plugin: Plugin, server: Server) {
            pickaxes.addRecipes("Hammer", plugin, server)
        }
    }
}
