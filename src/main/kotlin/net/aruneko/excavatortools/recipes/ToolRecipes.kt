package net.aruneko.excavatortools.recipes

import net.aruneko.excavatortools.tools.ToolIdentity
import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.Plugin

object ToolRecipes {
    fun register(plugin: Plugin, server: Server, toolIdentity: ToolIdentity) {
        for (type in ToolType.entries) {
            for (material in ToolMaterial.entries) {
                val tool = material.tool(type)
                val item = ItemStack(tool)
                val meta = requireNotNull(item.itemMeta)
                meta.setDisplayName("${ChatColor.GREEN}${material.displayName} ${type.displayName}")
                meta.addEnchant(Enchantment.UNBREAKING, 1, true)
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                meta.lore = listOf("${type.name}!")
                toolIdentity.mark(meta, type)
                item.itemMeta = meta

                val key = NamespacedKey(plugin, "${material.id}.${type.id}")
                val recipe = ShapedRecipe(key, item)
                recipe.shape(" M ", "MTM", " M ")
                recipe.setIngredient('T', tool)
                recipe.setIngredient('M', RecipeChoice.MaterialChoice(material.ingredients(Tag.PLANKS)))
                server.addRecipe(recipe)
            }
        }
    }
}
