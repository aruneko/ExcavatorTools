package net.aruneko.excavatortools.recipes

import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.Material
import org.bukkit.Tag

enum class ToolMaterial(
    val id: String,
    val displayName: String,
    private val pickaxe: Material,
    private val shovel: Material,
    private val fixedIngredients: List<Material>,
) {
    WOODEN("wooden", "Wooden", Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL, emptyList()),
    STONE("stone", "Stone", Material.STONE_PICKAXE, Material.STONE_SHOVEL,
        listOf(Material.COBBLESTONE, Material.BLACKSTONE)),
    IRON("iron", "Iron", Material.IRON_PICKAXE, Material.IRON_SHOVEL, listOf(Material.IRON_INGOT)),
    GOLDEN("golden", "Golden", Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, listOf(Material.GOLD_INGOT)),
    DIAMOND("diamond", "Diamond", Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, listOf(Material.DIAMOND)),
    NETHERITE("netherite", "Netherite", Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL,
        listOf(Material.NETHERITE_INGOT));

    fun tool(type: ToolType): Material = when (type) {
        ToolType.HAMMER -> pickaxe
        ToolType.EXCAVATOR -> shovel
    }

    fun ingredients(planks: Tag<Material>): List<Material> = when (this) {
        WOODEN -> planks.values.toList()
        else -> fixedIngredients
    }
}
