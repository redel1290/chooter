package com.shootergame.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class Joystick(
    private val baseX: Float,
    private val baseY: Float,
    private val baseRadius: Float,
    private val knobRadius: Float
) {
    var dx = 0f; var dy = 0f
    var active = false
    private var touchId = -1
    private var knobX = baseX; var knobY = baseY

    val angle get() = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
    val magnitude get() = Vector2(dx, dy).len()
    val isActive get() = active && magnitude > 0.05f

    fun update() {
        var found = false
        for (i in 0 until 5) {
            if (!Gdx.input.isTouched(i)) continue
            val tx = Gdx.input.getX(i).toFloat()
            val ty = (Gdx.graphics.height - Gdx.input.getY(i)).toFloat()

            val dist = Vector2.dst(tx, ty, baseX, baseY)
            if (!active && dist <= baseRadius * 1.5f) {
                touchId = i; active = true
            }
            if (active && touchId == i) {
                val delta = Vector2(tx - baseX, ty - baseY)
                if (delta.len() > baseRadius) delta.nor().scl(baseRadius)
                knobX = baseX + delta.x
                knobY = baseY + delta.y
                dx = delta.x / baseRadius
                dy = delta.y / baseRadius
                found = true
            }
        }
        if (!found && active) {
            active = false; touchId = -1
            dx = 0f; dy = 0f
            knobX = baseX; knobY = baseY
        }
    }

    fun draw(shape: ShapeRenderer) {
        // Base ring
        shape.color = Color(1f, 1f, 1f, 0.12f)
        shape.circle(baseX, baseY, baseRadius)

        // Inner fill
        shape.color = Color(1f, 1f, 1f, 0.06f)
        shape.circle(baseX, baseY, baseRadius * 0.6f)

        // Knob
        shape.color = if (active) Color(0.4f, 0.7f, 1f, 0.7f)
                      else Color(1f, 1f, 1f, 0.3f)
        shape.circle(knobX, knobY, knobRadius)
    }
}
