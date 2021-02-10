package net.aruneko.excavatortools.recipes;

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.Plugin

data class MaterialPair(val materialName: String, val tool: Material, val material: Material)

fun List<MaterialPair>.addRecipes(itemName: String, plugin: Plugin, server: Server) {
    this.forEach{
        val item = ItemStack(it.tool)
        val meta = item.itemMeta
        meta?.setDisplayName("${ChatColor.GREEN}${it.materialName} $itemName")
        meta?.addEnchant(Enchantment.DURABILITY, 1, true)
        meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        meta?.lore = listOf("${itemName.toUpperCase()}!")
        item.itemMeta = meta

        val key = NamespacedKey(plugin, "${it.material.name}.$itemName")
        val recipe = ShapedRecipe(key, item)
        recipe.shape(" M ", "MTM", " M ")
        recipe.setIngredient('T', it.tool)
        recipe.setIngredient('M', it.material)

        server.addRecipe(recipe)
    }
}
