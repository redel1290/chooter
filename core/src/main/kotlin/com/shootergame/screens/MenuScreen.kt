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

class MenuScreen(private val game: ShooterGame) : Screen {

    private val viewport = FitViewport(ShooterGame.VIRTUAL_WIDTH, ShooterGame.VIRTUAL_HEIGHT)
    private val shape    = ShapeRenderer()
    private val layout   = GlyphLayout()

    // Button rects [x, y, w, h]
    private val btnPlay     = floatArrayOf(490f, 340f, 300f, 70f)
    private val btnSettings = floatArrayOf(490f, 250f, 300f, 70f)
    private val btnQuit     = floatArrayOf(490f, 160f, 300f, 70f)

    override fun show() {}

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        viewport.apply()
        val cam = viewport.camera
        cam.update()

        // Draw buttons
        shape.projectionMatrix = cam.combined
        shape.begin(ShapeRenderer.ShapeType.Filled)

        drawButton(shape, btnPlay,     Color(0.2f, 0.6f, 0.9f, 1f))
        drawButton(shape, btnSettings, Color(0.3f, 0.3f, 0.4f, 1f))
        drawButton(shape, btnQuit,     Color(0.7f, 0.2f, 0.2f, 1f))

        shape.end()

        // Draw text
        game.batch.projectionMatrix = cam.combined
        game.batch.begin()

        // Title
        game.font.data.setScale(3f)
        game.font.color = Color.WHITE
        layout.setText(game.font, "SHOOTER GAME")
        game.font.draw(game.batch, "SHOOTER GAME",
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f, 560f)

        // Version
        game.font.data.setScale(1f)
        game.font.color = Color(0.5f, 0.5f, 0.5f, 1f)
        game.font.draw(game.batch, ShooterGame.VERSION, 10f, 20f)

        // Button labels
        game.font.data.setScale(1.8f)
        game.font.color = Color.WHITE
        drawButtonLabel("ГРАТИ",         btnPlay)
        drawButtonLabel("НАЛАШТУВАННЯ",  btnSettings)
        drawButtonLabel("ВИЙТИ",         btnQuit)

        game.batch.end()

        handleInput()
    }

    private fun drawButton(s: ShapeRenderer, b: FloatArray, color: Color) {
        s.color = color
        s.rect(b[0], b[1], b[2], b[3])
    }

    private fun drawButtonLabel(text: String, b: FloatArray) {
        layout.setText(game.font, text)
        game.font.draw(game.batch, text,
            b[0] + b[2] / 2f - layout.width / 2f,
            b[1] + b[3] / 2f + layout.height / 2f)
    }

    private fun handleInput() {
        if (!Gdx.input.justTouched()) return
        val touch = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
        when {
            inButton(touch, btnPlay)     -> game.setScreen(LobbyScreen(game))
            inButton(touch, btnSettings) -> game.setScreen(SettingsScreen(game))
            inButton(touch, btnQuit)     -> Gdx.app.exit()
        }
    }

    private fun inButton(p: Vector2, b: FloatArray) =
        p.x >= b[0] && p.x <= b[0] + b[2] && p.y >= b[1] && p.y <= b[1] + b[3]

    override fun resize(width: Int, height: Int) { viewport.update(width, height, true) }
    override fun pause()   {}
    override fun resume()  {}
    override fun hide()    {}
    override fun dispose() { shape.dispose() }
}
