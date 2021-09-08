package net.aruneko.excavatortools.recipes

import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class Excavator {
    companion object {
        private val shovels = listOf(
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.OAK_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.ACACIA_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.BIRCH_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.DARK_OAK_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.JUNGLE_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.SPRUCE_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.CRIMSON_PLANKS),
            MaterialPair("Wooden", Material.WOODEN_SHOVEL, Material.WARPED_PLANKS),
            MaterialPair("Stone", Material.STONE_SHOVEL, Material.COBBLESTONE),
            MaterialPair("Stone", Material.STONE_SHOVEL, Material.BLACKSTONE),
            MaterialPair("Iron", Material.IRON_SHOVEL, Material.IRON_INGOT),
            MaterialPair("Golden", Material.GOLDEN_SHOVEL, Material.GOLD_INGOT),
            MaterialPair("Diamond", Material.DIAMOND_SHOVEL, Material.DIAMOND),
            MaterialPair("Netherite", Material.NETHERITE_SHOVEL, Material.NETHERITE_INGOT),
        )

        private fun ItemStack.isShovel(): Boolean {
            return shovels.map{ it.tool }.contains(this.type)
        }

        fun ItemStack.isExcavator(): Boolean {
            return this.isShovel() && this.itemMeta?.lore?.contains("EXCAVATOR!") ?: false
        }

        fun addRecipes(plugin: Plugin, server: Server) {
            shovels.addRecipes("Excavator", plugin, server)
        }
    }
}
