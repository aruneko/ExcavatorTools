package net.aruneko.excavatortools

import net.aruneko.excavatortools.listeners.ExcavatorListener
import net.aruneko.excavatortools.listeners.HammerListener
import net.aruneko.excavatortools.recipes.Excavator
import net.aruneko.excavatortools.recipes.Hammer
import org.bukkit.plugin.java.JavaPlugin

class ExcavatorTools : JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(ExcavatorListener(this, server), this)
        server.pluginManager.registerEvents(HammerListener(this, server), this)
        Hammer.addRecipes(this, server)
        Excavator.addRecipes(this, server)
    }

    override fun onDisable() {}
}
