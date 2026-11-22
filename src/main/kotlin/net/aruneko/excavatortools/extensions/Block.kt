package net.aruneko.excavatortools.extensions

import org.bukkit.block.Block
import org.bukkit.block.BlockFace

fun Block.surroundingBlocks(face: BlockFace): List<Block> = buildList {
    for (a in -1..1) {
        for (b in -1..1) {
            if (a == 0 && b == 0) continue

            val block = when (face) {
                BlockFace.UP, BlockFace.DOWN -> getRelative(a, 0, b)
                BlockFace.NORTH, BlockFace.SOUTH -> getRelative(a, b, 0)
                BlockFace.EAST, BlockFace.WEST -> getRelative(0, a, b)
                else -> continue
            }
            add(block)
        }
    }
}
