package net.aruneko.excavatortools

import net.aruneko.excavatortools.recipes.ToolMaterial
import net.aruneko.excavatortools.tools.ToolType
import org.bukkit.Material
import org.bukkit.Tag
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToolMaterialTest {
    private val planks = setOf(Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.CHERRY_PLANKS)
    @Suppress("UNCHECKED_CAST")
    private val plankTag = Proxy.newProxyInstance(Tag::class.java.classLoader, arrayOf(Tag::class.java)) { _, method, _ ->
        when (method.name) {
            "getValues" -> planks
            else -> error("Unexpected Tag.${method.name}")
        }
    } as Tag<Material>

    @Test
    fun `wooden recipe accepts all supplied planks including mixed inputs`() {
        val choice = ToolMaterial.WOODEN.ingredients(plankTag)
        assertEquals(planks, choice.toSet())
        val mixed = listOf(Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.CHERRY_PLANKS, Material.OAK_PLANKS)
        assertTrue(mixed.all { it in choice })
        assertFalse(Material.OAK_LOG in choice)
    }

    @Test
    fun `stone recipe accepts mixed cobblestone and blackstone only`() {
        val choice = ToolMaterial.STONE.ingredients(plankTag)
        assertTrue(listOf(Material.COBBLESTONE, Material.BLACKSTONE, Material.COBBLESTONE, Material.BLACKSTONE)
            .all { it in choice })
        assertEquals(setOf(Material.COBBLESTONE, Material.BLACKSTONE), choice.toSet())
        assertFalse(Material.DIRT in choice)
    }

    @Test
    fun `other tiers keep their original ingredients`() {
        for ((tier, ingredient) in mapOf(
            ToolMaterial.IRON to Material.IRON_INGOT,
            ToolMaterial.GOLDEN to Material.GOLD_INGOT,
            ToolMaterial.DIAMOND to Material.DIAMOND,
            ToolMaterial.NETHERITE to Material.NETHERITE_INGOT,
        )) {
            assertEquals(listOf(ingredient), tier.ingredients(plankTag))
        }
    }

    @Test
    fun `each tier produces distinct pickaxe and shovel recipes`() {
        val tools = ToolMaterial.entries.flatMap { tier -> ToolType.entries.map { tier.tool(it) } }
        assertEquals(12, tools.toSet().size)
        for (tier in ToolMaterial.entries) {
            assertTrue(tier.tool(ToolType.HAMMER).name.endsWith("_PICKAXE"))
            assertTrue(tier.tool(ToolType.EXCAVATOR).name.endsWith("_SHOVEL"))
        }
    }
}
