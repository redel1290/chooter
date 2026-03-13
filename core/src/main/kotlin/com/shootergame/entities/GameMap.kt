package com.shootergame.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle

data class Wall(val x: Float, val y: Float, val w: Float, val h: Float) {
    val rect get() = Rectangle(x, y, w, h)
}

data class Crate(val x: Float, val y: Float, val size: Float = 48f) {
    val rect get() = Rectangle(x - size / 2f, y - size / 2f, size, size)
}

class GameMap(val index: Int) {

    companion object {
        const val W = 1280f
        const val H = 720f
        const val WALL_THICK = 24f
    }

    val walls  = mutableListOf<Wall>()
    val crates = mutableListOf<Crate>()
    val spawn1: Pair<Float, Float>
    val spawn2: Pair<Float, Float>
    val name: String

    init {
        // Outer border walls (all maps)
        walls += Wall(0f, 0f, W, WALL_THICK)            // bottom
        walls += Wall(0f, H - WALL_THICK, W, WALL_THICK) // top
        walls += Wall(0f, 0f, WALL_THICK, H)             // left
        walls += Wall(W - WALL_THICK, 0f, WALL_THICK, H) // right

        when (index) {
            0 -> { // Arena — open with crates
                name   = "Арена"
                spawn1 = Pair(120f, 360f)
                spawn2 = Pair(1160f, 360f)

                // Central cross cover
                crates += Crate(640f, 360f)
                crates += Crate(640f, 290f)
                crates += Crate(640f, 430f)
                crates += Crate(570f, 360f)
                crates += Crate(710f, 360f)

                // Corner groups
                crates += Crate(260f, 560f)
                crates += Crate(310f, 560f)
                crates += Crate(260f, 510f)

                crates += Crate(1020f, 560f)
                crates += Crate(970f, 560f)
                crates += Crate(1020f, 510f)

                crates += Crate(260f, 200f)
                crates += Crate(310f, 200f)
                crates += Crate(260f, 150f)

                crates += Crate(1020f, 200f)
                crates += Crate(970f, 200f)
                crates += Crate(1020f, 150f)
            }
            else -> { // Maze — corridors
                name   = "Лабіринт"
                spawn1 = Pair(120f, 120f)
                spawn2 = Pair(1160f, 600f)

                // Horizontal walls (corridors)
                walls += Wall(200f, 500f, 300f, WALL_THICK)
                walls += Wall(600f, 500f, 280f, WALL_THICK)
                walls += Wall(200f, 340f, 200f, WALL_THICK)
                walls += Wall(780f, 340f, 300f, WALL_THICK)
                walls += Wall(400f, 180f, 300f, WALL_THICK)
                walls += Wall(800f, 180f, 200f, WALL_THICK)

                // Vertical walls
                walls += Wall(500f, 340f, WALL_THICK, 200f)
                walls += Wall(780f, 180f, WALL_THICK, 200f)
                walls += Wall(640f, 400f, WALL_THICK, 150f)

                // Some crates for extra cover
                crates += Crate(360f, 420f)
                crates += Crate(880f, 280f)
                crates += Crate(640f, 260f)
            }
        }
    }

    fun drawFloor(shape: ShapeRenderer) {
        shape.color = Color(0.13f, 0.13f, 0.18f, 1f)
        shape.rect(WALL_THICK, WALL_THICK, W - WALL_THICK * 2f, H - WALL_THICK * 2f)

        // Grid lines
        shape.color = Color(0.16f, 0.16f, 0.22f, 1f)
        var gx = WALL_THICK
        while (gx < W - WALL_THICK) { shape.rectLine(gx, WALL_THICK, gx, H - WALL_THICK, 1f); gx += 64f }
        var gy = WALL_THICK
        while (gy < H - WALL_THICK) { shape.rectLine(WALL_THICK, gy, W - WALL_THICK, gy, 1f); gy += 64f }
    }

    fun drawWalls(shape: ShapeRenderer) {
        walls.forEach { w ->
            // Wall shadow
            shape.color = Color(0f, 0f, 0f, 0.4f)
            shape.rect(w.x + 3f, w.y - 3f, w.w, w.h)
            // Wall body
            shape.color = Color(0.28f, 0.28f, 0.35f, 1f)
            shape.rect(w.x, w.y, w.w, w.h)
            // Wall highlight
            shape.color = Color(0.38f, 0.38f, 0.45f, 1f)
            shape.rect(w.x, w.y + w.h - 4f, w.w, 4f)
        }
    }

    fun drawCrates(shape: ShapeRenderer) {
        crates.forEach { c ->
            val r = c.rect
            // Shadow
            shape.color = Color(0f, 0f, 0f, 0.35f)
            shape.rect(r.x + 4f, r.y - 4f, r.width, r.height)
            // Crate body
            shape.color = Color(0.55f, 0.38f, 0.18f, 1f)
            shape.rect(r.x, r.y, r.width, r.height)
            // Cross lines
            shape.color = Color(0.42f, 0.28f, 0.12f, 1f)
            shape.rectLine(r.x, r.y, r.x + r.width, r.y + r.height, 2f)
            shape.rectLine(r.x + r.width, r.y, r.x, r.y + r.height, 2f)
            shape.rect(r.x, r.y + r.height - 3f, r.width, 3f)
            shape.rect(r.x, r.y, r.width, 3f)
        }
    }

    fun collidesWithWall(x: Float, y: Float, radius: Float): Boolean {
        return walls.any { w ->
            val nearX = x.coerceIn(w.x, w.x + w.w)
            val nearY = y.coerceIn(w.y, w.y + w.h)
            val dx = x - nearX; val dy = y - nearY
            dx * dx + dy * dy < radius * radius
        } || crates.any { c ->
            val r = c.rect
            val nearX = x.coerceIn(r.x, r.x + r.width)
            val nearY = y.coerceIn(r.y, r.y + r.height)
            val dx = x - nearX; val dy = y - nearY
            dx * dx + dy * dy < radius * radius
        }
    }

    fun bulletHitsWall(bx: Float, by: Float): Boolean {
        return walls.any { w -> w.rect.contains(bx, by) } ||
               crates.any { c -> c.rect.contains(bx, by) }
    }
}
