package net.aruneko.excavatortools.extensions

import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

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
