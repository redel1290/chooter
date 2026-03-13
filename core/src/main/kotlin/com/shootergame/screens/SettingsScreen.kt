package com.shootergame.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.math.Vector2
import com.shootergame.ShooterGame
import com.shootergame.utils.GameSettings

class SettingsScreen(private val game: ShooterGame) : Screen {

    private val viewport = FitViewport(ShooterGame.VIRTUAL_WIDTH, ShooterGame.VIRTUAL_HEIGHT)
    private val shape    = ShapeRenderer()
    private val layout   = GlyphLayout()

    // Tabs: 0=Sound, 1=Network, 2=Controls, 3=Graphics, 4=About
    private var activeTab = 0
    private val tabs = listOf("Sound", "Network", "Controls", "Graphics", "About")

    private val btnBack = floatArrayOf(40f, 30f, 180f, 60f)

    override fun show() {}

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply()
        val cam = viewport.camera; cam.update()

        shape.projectionMatrix = cam.combined
        shape.begin(ShapeRenderer.ShapeType.Filled)
        drawTabs()
        drawContent()
        shape.color = Color(0.4f, 0.4f, 0.5f, 1f)
        shape.rect(btnBack[0], btnBack[1], btnBack[2], btnBack[3])
        shape.end()

        game.batch.projectionMatrix = cam.combined
        game.batch.begin()
        drawTextOverlay()
        game.batch.end()

        handleInput()
    }

    private fun drawTabs() {
        val tabW = 220f; val tabH = 60f; val startX = 90f; val tabY = 620f
        tabs.forEachIndexed { i, _ ->
            shape.color = if (i == activeTab) Color(0.2f, 0.6f, 0.9f, 1f)
                          else Color(0.2f, 0.2f, 0.28f, 1f)
            shape.rect(startX + i * (tabW + 10f), tabY, tabW, tabH)
        }
    }

    private fun drawContent() {
        // Slider tracks
        shape.color = Color(0.2f, 0.2f, 0.28f, 1f)
        when (activeTab) {
            0 -> { // Sound
                shape.rect(400f, 480f, 400f, 20f) // Music vol
                shape.rect(400f, 380f, 400f, 20f) // SFX vol
                // Filled portions
                shape.color = Color(0.2f, 0.6f, 0.9f, 1f)
                shape.rect(400f, 480f, 400f * GameSettings.musicVolume, 20f)
                shape.rect(400f, 380f, 400f * GameSettings.sfxVolume, 20f)
                // Toggle
                val toggleColor = if (GameSettings.soundEnabled) Color(0.2f, 0.8f, 0.3f, 1f)
                                  else Color(0.6f, 0.2f, 0.2f, 1f)
                shape.color = toggleColor
                shape.rect(400f, 280f, 80f, 40f)
            }
            1 -> { // Network
                val btColor = if (GameSettings.useBluetooth) Color(0.2f, 0.4f, 0.9f, 1f)
                              else Color(0.2f, 0.2f, 0.28f, 1f)
                shape.color = btColor
                shape.rect(400f, 440f, 200f, 50f)
                shape.color = if (!GameSettings.useBluetooth) Color(0.2f, 0.6f, 0.4f, 1f)
                              else Color(0.2f, 0.2f, 0.28f, 1f)
                shape.rect(620f, 440f, 200f, 50f)
            }
            2 -> { // Controls
                // Joystick size buttons
                val sizes = listOf(0.7f, 1.0f, 1.3f)
                sizes.forEachIndexed { i, v ->
                    shape.color = if (GameSettings.joystickSize == v) Color(0.2f, 0.6f, 0.9f, 1f)
                                  else Color(0.2f, 0.2f, 0.28f, 1f)
                    shape.rect(300f + i * 160f, 440f, 140f, 60f)
                }
                // Left-handed toggle
                shape.color = if (GameSettings.leftHanded) Color(0.2f, 0.8f, 0.3f, 1f)
                              else Color(0.6f, 0.2f, 0.2f, 1f)
                shape.rect(400f, 320f, 80f, 40f)
            }
            3 -> { // Graphics
                shape.color = if (GameSettings.showFps) Color(0.2f, 0.8f, 0.3f, 1f)
                              else Color(0.6f, 0.2f, 0.2f, 1f)
                shape.rect(400f, 440f, 80f, 40f)
            }
        }
    }

    private fun drawTextOverlay() {
        game.font.data.setScale(2f)
        game.font.color = Color.WHITE
        layout.setText(game.font, "НАЛАШТУВАННЯ")
        game.font.draw(game.batch, "НАЛАШТУВАННЯ",
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f, 710f)

        game.font.data.setScale(1.3f)
        val tabW = 220f; val startX = 90f; val tabY = 660f
        tabs.forEachIndexed { i, name ->
            layout.setText(game.font, name)
            game.font.color = if (i == activeTab) Color.WHITE else Color(0.7f, 0.7f, 0.7f, 1f)
            game.font.draw(game.batch, name,
                startX + i * (tabW + 10f) + tabW / 2f - layout.width / 2f, tabY)
        }

        game.font.color = Color.WHITE
        game.font.data.setScale(1.4f)
        when (activeTab) {
            0 -> {
                game.font.draw(game.batch, "Гучність музики", 100f, 520f)
                game.font.draw(game.batch, "${(GameSettings.musicVolume * 100).toInt()}%", 820f, 510f)
                game.font.draw(game.batch, "Гучність ефектів", 100f, 420f)
                game.font.draw(game.batch, "${(GameSettings.sfxVolume * 100).toInt()}%", 820f, 410f)
                game.font.draw(game.batch, "Sound увімкнено", 100f, 320f)
                game.font.draw(game.batch, if (GameSettings.soundEnabled) "ВКЛ" else "ВИКЛ", 400f, 312f)
            }
            1 -> {
                game.font.draw(game.batch, "Метод з'єднання:", 100f, 530f)
                game.font.draw(game.batch, "Bluetooth", 410f, 480f)
                game.font.draw(game.batch, "Wi-Fi", 660f, 480f)
                game.font.color = Color(0.7f, 0.7f, 0.7f, 1f)
                game.font.data.setScale(1.1f)
                game.font.draw(game.batch, "Порт: ${GameSettings.networkPort}  (для просунутих)", 100f, 380f)
            }
            2 -> {
                game.font.draw(game.batch, "Розмір джойстиків:", 100f, 540f)
                val labels = listOf("Small", "Medium", "Large")
                labels.forEachIndexed { i, l ->
                    game.font.color = Color.WHITE
                    layout.setText(game.font, l)
                    game.font.draw(game.batch, l, 300f + i * 160f + 70f - layout.width / 2f, 482f)
                }
                game.font.draw(game.batch, "Режим лівші:", 100f, 370f)
                game.font.draw(game.batch, if (GameSettings.leftHanded) "ВКЛ" else "ВИКЛ", 400f, 352f)
            }
            3 -> {
                game.font.draw(game.batch, "Показувати FPS:", 100f, 500f)
                game.font.draw(game.batch, if (GameSettings.showFps) "ВКЛ" else "ВИКЛ", 400f, 472f)
                game.font.color = Color(0.6f, 0.6f, 0.6f, 1f)
                game.font.data.setScale(1.1f)
                game.font.draw(game.batch, "Більше графічних опцій буде в наступних версіях", 100f, 360f)
            }
            4 -> {
                game.font.draw(game.batch, "Версія: ${ShooterGame.VERSION}", 100f, 520f)
                game.font.color = Color(0.7f, 0.7f, 0.7f, 1f)
                game.font.data.setScale(1.1f)
                game.font.draw(game.batch, "Top-down 2D шутер", 100f, 460f)
                game.font.draw(game.batch, "Спрайти: Kenney.nl (CC0)", 100f, 420f)
                game.font.draw(game.batch, "Зроблено з LibGDX", 100f, 380f)
            }
        }

        game.font.data.setScale(1.4f)
        game.font.color = Color.WHITE
        drawLabel("← НАЗАД", btnBack)
    }

    private fun drawLabel(text: String, b: FloatArray) {
        layout.setText(game.font, text)
        game.font.draw(game.batch, text,
            b[0] + b[2] / 2f - layout.width / 2f,
            b[1] + b[3] / 2f + layout.height / 2f)
    }

    private fun handleInput() {
        if (!Gdx.input.justTouched()) return
        val t = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))

        if (inBtn(t, btnBack)) { game.setScreen(MenuScreen(game)); return }

        // Tab click
        val tabW = 220f; val startX = 90f; val tabY = 620f; val tabH = 60f
        tabs.forEachIndexed { i, _ ->
            if (t.x >= startX + i * (tabW + 10f) && t.x <= startX + i * (tabW + 10f) + tabW
                && t.y >= tabY && t.y <= tabY + tabH) activeTab = i
        }

        when (activeTab) {
            0 -> {
                if (t.y in 470f..510f && t.x in 400f..800f)
                    GameSettings.musicVolume = ((t.x - 400f) / 400f).coerceIn(0f, 1f)
                if (t.y in 370f..410f && t.x in 400f..800f)
                    GameSettings.sfxVolume = ((t.x - 400f) / 400f).coerceIn(0f, 1f)
                if (t.y in 270f..330f && t.x in 390f..490f)
                    GameSettings.soundEnabled = !GameSettings.soundEnabled
            }
            1 -> {
                if (t.y in 430f..500f && t.x in 390f..620f) GameSettings.useBluetooth = true
                if (t.y in 430f..500f && t.x in 610f..830f) GameSettings.useBluetooth = false
            }
            2 -> {
                val sizes = listOf(0.7f, 1.0f, 1.3f)
                sizes.forEachIndexed { i, v ->
                    if (t.y in 430f..510f && t.x in 300f + i * 160f..440f + i * 160f)
                        GameSettings.joystickSize = v
                }
                if (t.y in 310f..370f && t.x in 390f..490f)
                    GameSettings.leftHanded = !GameSettings.leftHanded
            }
            3 -> {
                if (t.y in 430f..490f && t.x in 390f..490f)
                    GameSettings.showFps = !GameSettings.showFps
            }
        }
    }

    private fun inBtn(p: Vector2, b: FloatArray) =
        p.x >= b[0] && p.x <= b[0] + b[2] && p.y >= b[1] && p.y <= b[1] + b[3]

    override fun resize(width: Int, height: Int) { viewport.update(width, height, true) }
    override fun pause()   {}
    override fun resume()  {}
    override fun hide()    {}
    override fun dispose() { shape.dispose() }
}
