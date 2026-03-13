package com.shootergame.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Circle

class Bullet(
    x: Float, y: Float,
    angle: Float,
    val ownerId: Int,
    val weaponType: Int  // 0=pistol, 1=shotgun pellet
) {
    companion object {
        const val PISTOL_SPEED  = 600f
        const val SHOTGUN_SPEED = 480f
        const val PISTOL_DAMAGE = 20
        const val SHOTGUN_DAMAGE = 15
        const val PISTOL_RANGE  = 800f
        const val SHOTGUN_RANGE = 320f
        const val RADIUS = 5f
    }

    var x = x; var y = y
    var alive = true
    private var distanceTravelled = 0f

    private val speed  = if (weaponType == 0) PISTOL_SPEED  else SHOTGUN_SPEED
    val damage = if (weaponType == 0) PISTOL_DAMAGE else SHOTGUN_DAMAGE
    private val range  = if (weaponType == 0) PISTOL_RANGE  else SHOTGUN_RANGE

    private val vx = Math.cos(Math.toRadians(angle.toDouble())).toFloat() * speed
    private val vy = Math.sin(Math.toRadians(angle.toDouble())).toFloat() * speed

    // Trail positions
    private val trail = ArrayDeque<Vector2>(5)

    val bounds get() = Circle(x, y, RADIUS)

    fun update(delta: Float) {
        if (!alive) return
        trail.addFirst(Vector2(x, y))
        if (trail.size > 5) trail.removeLast()

        x += vx * delta
        y += vy * delta
        distanceTravelled += speed * delta
        if (distanceTravelled >= range) alive = false
    }

    fun draw(shape: ShapeRenderer) {
        if (!alive) return

        // Trail
        trail.forEachIndexed { i, pos ->
            val alpha = (1f - i / 5f) * 0.5f
            shape.color = if (weaponType == 0)
                Color(1f, 0.9f, 0.4f, alpha)
            else
                Color(1f, 0.5f, 0.1f, alpha)
            val r = RADIUS * (1f - i / 6f)
            shape.circle(pos.x, pos.y, r)
        }

        // Bullet
        shape.color = if (weaponType == 0) Color(1f, 1f, 0.6f, 1f)
                      else Color(1f, 0.7f, 0.2f, 1f)
        shape.circle(x, y, RADIUS)
    }
}
