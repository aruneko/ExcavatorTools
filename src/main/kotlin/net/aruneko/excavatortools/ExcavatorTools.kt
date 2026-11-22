package net.aruneko.excavatortools

import net.aruneko.excavatortools.listeners.MiningFaceListener
import net.aruneko.excavatortools.listeners.MiningListener
import net.aruneko.excavatortools.recipes.ToolRecipes
import net.aruneko.excavatortools.tools.ToolIdentity
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

class ExcavatorTools : JavaPlugin() {
    private val miningFaces = MiningFaceListener()

    override fun onEnable() {
        val toolIdentity = ToolIdentity(NamespacedKey(this, "tool_type"))
        server.pluginManager.registerEvents(miningFaces, this)
        server.pluginManager.registerEvents(MiningListener(miningFaces, toolIdentity), this)
        ToolRecipes.register(this, server, toolIdentity)
    }

    override fun onDisable() {
        miningFaces.clear()
    }
}
