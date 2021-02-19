package net.aruneko.excavatortools.extensions

import net.minecraft.server.v1_16_R3.Blocks
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun Block.facingAirBlocs(): List<Block> {
    val world = this.world
    val x = this.x
    val y = this.y
    val z = this.z

    val facingBlocks = listOf(
        world.getBlockAt(x + 1, y, z),
        world.getBlockAt(x - 1, y, z),
        world.getBlockAt(x, y + 1, z),
        world.getBlockAt(x, y - 1, z),
        world.getBlockAt(x, y, z + 1),
        world.getBlockAt(x, y, z - 1),
    )

    return facingBlocks.filter { it.type.isAir }
}

fun List<Block>.findNearest(player: Player): Block {
    val playerLocation = player.location
    return this.sortedBy { it.location.distance(playerLocation) }[0]
}

fun Block.surroundingBlocks(player: Player): List<Block> {
    val world = this.world
    val x = this.x
    val y = this.y
    val z = this.z

    val virtualFacingBlock = this.facingAirBlocs().findNearest(player)

    return when(this.getFace(virtualFacingBlock)) {
        BlockFace.NORTH,
        BlockFace.SOUTH -> listOf(
            world.getBlockAt(x + 1, y, z),
            world.getBlockAt(x - 1, y, z),
            world.getBlockAt(x + 1, y + 1, z),
            world.getBlockAt(x, y + 1, z),
            world.getBlockAt(x - 1, y + 1, z),
            world.getBlockAt(x + 1, y - 1, z),
            world.getBlockAt(x, y - 1, z),
            world.getBlockAt(x - 1, y - 1, z),
        )
        BlockFace.EAST,
        BlockFace.WEST -> listOf(
            world.getBlockAt(x, y, z + 1),
            world.getBlockAt(x, y, z - 1),
            world.getBlockAt(x, y + 1, z + 1),
            world.getBlockAt(x, y + 1, z),
            world.getBlockAt(x, y + 1, z - 1),
            world.getBlockAt(x, y - 1, z + 1),
            world.getBlockAt(x, y - 1, z),
            world.getBlockAt(x, y - 1, z - 1),
        )
        BlockFace.UP,
        BlockFace.DOWN -> listOf(
            world.getBlockAt(x + 1, y, z - 1),
            world.getBlockAt(x + 1, y, z),
            world.getBlockAt(x + 1, y, z + 1),
            world.getBlockAt(x, y, z - 1),
            world.getBlockAt(x, y, z + 1),
            world.getBlockAt(x - 1, y, z - 1),
            world.getBlockAt(x - 1, y, z),
            world.getBlockAt(x - 1, y, z + 1),
        )
        else -> listOf(this)
    }
}

fun Block.canBreakWith(tool: ItemStack): Boolean {
    val nmsBlock = CraftMagicNumbers.getBlock(this.type)
    val blockData = nmsBlock.blockData
    val nmsItem = CraftMagicNumbers.getItem(tool.type)

    return nmsItem.canDestroySpecialBlock(blockData)
}

fun Block.canBreakWithShovel(): Boolean {
    val blocks = listOf(
        Blocks.CLAY, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.FARMLAND, Blocks.GRASS_BLOCK, Blocks.GRAVEL,
        Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND, Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.SOUL_SAND,
        Blocks.GRASS_PATH, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER,
        Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER,
        Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER,
        Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER,
        Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER,
        Blocks.BLACK_CONCRETE_POWDER, Blocks.SOUL_SOIL
    )
    val nmsBlock = CraftMagicNumbers.getBlock(this.type)
    return blocks.contains(nmsBlock)
}
