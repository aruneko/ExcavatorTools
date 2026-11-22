package net.aruneko.excavatortools

import net.aruneko.excavatortools.listeners.MiningFaceListener
import net.aruneko.excavatortools.listeners.MiningListener
import net.aruneko.excavatortools.recipes.Excavator
import net.aruneko.excavatortools.recipes.Hammer
import org.bukkit.plugin.java.JavaPlugin

class ExcavatorTools : JavaPlugin() {
    private val miningFaces = MiningFaceListener()

    override fun onEnable() {
        server.pluginManager.registerEvents(miningFaces, this)
        server.pluginManager.registerEvents(MiningListener(miningFaces), this)
        Hammer.addRecipes(this, server)
        Excavator.addRecipes(this, server)
    }

    override fun onDisable() {
        miningFaces.clear()
    }
}
