package com.shootergame.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Circle

class Player(
    var x: Float,
    var y: Float,
    val isLocal: Boolean,
    val weaponIndex: Int,
    val color: Color
) {
    companion object {
        const val RADIUS    = 24f
        const val SPEED     = 220f
        const val MAX_HP    = 100
    }

    var angle    = 0f
    var hp       = MAX_HP
    var shooting = false
    var alive    = true

    val velocity = Vector2()
    val position get() = Vector2(x, y)
    val bounds   get() = Circle(x, y, RADIUS)

    // Animation
    private var animTime  = 0f
    private var legAngle  = 0f

    fun update(delta: Float, dx: Float, dy: Float, aimAngle: Float, isShooting: Boolean) {
        if (!alive) return

        val move = Vector2(dx, dy)
        if (move.len() > 1f) move.nor()

        velocity.set(move.x * SPEED, move.y * SPEED)
        x += velocity.x * delta
        y += velocity.y * delta

        angle    = aimAngle
        shooting = isShooting

        if (move.len() > 0.1f) {
            animTime += delta
            legAngle = Math.sin(animTime * 12.0).toFloat() * 20f
        } else {
            animTime = 0f
            legAngle = 0f
        }
    }

    fun takeDamage(amount: Int) {
        hp = (hp - amount).coerceAtLeast(0)
        if (hp <= 0) alive = false
    }

    fun respawn(spawnX: Float, spawnY: Float) {
        x = spawnX; y = spawnY
        hp = MAX_HP; alive = true; shooting = false
    }

    fun draw(shape: ShapeRenderer) {
        if (!alive) return

        // Shadow
        shape.color = Color(0f, 0f, 0f, 0.3f)
        shape.ellipse(x - RADIUS * 0.8f, y - RADIUS * 0.5f, RADIUS * 1.6f, RADIUS * 0.8f)

        // Body
        shape.color = color
        shape.circle(x, y, RADIUS)

        // Darker center
        shape.color = Color(color.r * 0.7f, color.g * 0.7f, color.b * 0.7f, 1f)
        shape.circle(x, y, RADIUS * 0.55f)

        // Direction indicator (gun direction)
        val rad = Math.toRadians(angle.toDouble())
        val gunLen = RADIUS + 18f
        val ex = x + Math.cos(rad).toFloat() * gunLen
        val ey = y + Math.sin(rad).toFloat() * gunLen
        shape.color = Color.DARK_GRAY
        shape.rectLine(x, y, ex, ey, 6f)

        // Gun tip
        shape.color = Color.LIGHT_GRAY
        shape.circle(ex, ey, 4f)

        // HP bar background
        val barW = 50f; val barH = 6f
        val barX = x - barW / 2f; val barY = y + RADIUS + 8f
        shape.color = Color(0.3f, 0.1f, 0.1f, 0.9f)
        shape.rect(barX, barY, barW, barH)

        // HP bar fill
        val hpFrac = hp.toFloat() / MAX_HP
        val hpColor = when {
            hpFrac > 0.6f -> Color(0.2f, 0.9f, 0.2f, 1f)
            hpFrac > 0.3f -> Color(0.9f, 0.8f, 0.1f, 1f)
            else          -> Color(0.9f, 0.2f, 0.1f, 1f)
        }
        shape.color = hpColor
        shape.rect(barX, barY, barW * hpFrac, barH)
    }
}
