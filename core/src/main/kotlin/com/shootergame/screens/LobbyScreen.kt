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
import com.shootergame.network.NetworkManager
import com.shootergame.network.NetworkRole

class LobbyScreen(private val game: ShooterGame) : Screen {

    private val viewport = FitViewport(ShooterGame.VIRTUAL_WIDTH, ShooterGame.VIRTUAL_HEIGHT)
    private val shape    = ShapeRenderer()
    private val layout   = GlyphLayout()

    // State: null = role select, HOST = host waiting, CLIENT = enter IP
    private var role: NetworkRole? = null
    private var ipInput   = StringBuilder()
    private var statusMsg = ""
    private var connected = false

    // Weapon selection (0=pistol, 1=shotgun)
    private var myWeapon   = 0
    private var p2Weapon   = 0
    private var selectedMap = 0 // 0=Arena, 1=Maze
    private val weapons = listOf("Pistol", "Shotgun")
    private val maps    = listOf("Арена", "Лабіринт")

    // Buttons
    private val btnHost   = floatArrayOf(290f, 300f, 280f, 80f)
    private val btnJoin   = floatArrayOf(710f, 300f, 280f, 80f)
    private val btnBack   = floatArrayOf(40f,  30f,  180f, 60f)
    private val btnStart  = floatArrayOf(490f, 30f,  300f, 70f)
    private val btnWepL   = floatArrayOf(290f, 200f, 50f,  50f)
    private val btnWepR   = floatArrayOf(540f, 200f, 50f,  50f)
    private val btnMapL   = floatArrayOf(690f, 200f, 50f,  50f)
    private val btnMapR   = floatArrayOf(940f, 200f, 50f,  50f)

    private var networkManager: NetworkManager? = null

    override fun show() {}

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply()
        val cam = viewport.camera; cam.update()

        shape.projectionMatrix = cam.combined
        shape.begin(ShapeRenderer.ShapeType.Filled)

        when (role) {
            null -> drawRoleSelect()
            NetworkRole.HOST -> drawHostWaiting()
            NetworkRole.CLIENT -> drawClientConnect()
        }

        shape.end()

        game.batch.projectionMatrix = cam.combined
        game.batch.begin()
        drawText()
        game.batch.end()

        handleInput()
        networkManager?.update()
    }

    private fun drawRoleSelect() {
        shape.color = Color(0.2f, 0.6f, 0.9f, 1f)
        shape.rect(btnHost[0], btnHost[1], btnHost[2], btnHost[3])
        shape.color = Color(0.2f, 0.8f, 0.4f, 1f)
        shape.rect(btnJoin[0], btnJoin[1], btnJoin[2], btnJoin[3])
        shape.color = Color(0.4f, 0.4f, 0.5f, 1f)
        shape.rect(btnBack[0], btnBack[1], btnBack[2], btnBack[3])
    }

    private fun drawHostWaiting() {
        shape.color = Color(0.4f, 0.4f, 0.5f, 1f)
        shape.rect(btnBack[0], btnBack[1], btnBack[2], btnBack[3])
        if (connected) {
            // Weapon arrows
            shape.color = Color(0.3f, 0.3f, 0.5f, 1f)
            shape.rect(btnWepL[0], btnWepL[1], btnWepL[2], btnWepL[3])
            shape.rect(btnWepR[0], btnWepR[1], btnWepR[2], btnWepR[3])
            // Map arrows
            shape.rect(btnMapL[0], btnMapL[1], btnMapL[2], btnMapL[3])
            shape.rect(btnMapR[0], btnMapR[1], btnMapR[2], btnMapR[3])
            // Start button
            shape.color = Color(0.8f, 0.4f, 0.1f, 1f)
            shape.rect(btnStart[0], btnStart[1], btnStart[2], btnStart[3])
        }
    }

    private fun drawClientConnect() {
        shape.color = Color(0.15f, 0.15f, 0.2f, 1f)
        shape.rect(390f, 340f, 500f, 70f) // IP input field
        shape.color = Color(0.4f, 0.4f, 0.5f, 1f)
        shape.rect(btnBack[0], btnBack[1], btnBack[2], btnBack[3])
        if (!connected) {
            shape.color = Color(0.2f, 0.6f, 0.9f, 1f)
            shape.rect(490f, 250f, 300f, 70f) // Connect btn
        }
        if (connected) {
            shape.color = Color(0.3f, 0.3f, 0.5f, 1f)
            shape.rect(btnWepL[0], btnWepL[1], btnWepL[2], btnWepL[3])
            shape.rect(btnWepR[0], btnWepR[1], btnWepR[2], btnWepR[3])
        }
    }

    private fun drawText() {
        game.font.data.setScale(2.2f)
        game.font.color = Color.WHITE
        layout.setText(game.font, "ЛОБІ")
        game.font.draw(game.batch, "ЛОБІ", ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f, 660f)

        game.font.data.setScale(1.5f)

        when (role) {
            null -> {
                drawLabel("🏠  ХОСТ",      btnHost)
                drawLabel("🔗  ПІДКЛЮЧИТИСЬ", btnJoin)
                drawLabel("← НАЗАД",        btnBack)
            }
            NetworkRole.HOST -> {
                val ip = networkManager?.localIp ?: "—"
                game.font.color = Color(0.8f, 0.8f, 0.8f, 1f)
                game.font.draw(game.batch, "Your IP: $ip", 390f, 580f)
                if (!connected) {
                    game.font.color = Color(0.6f, 0.8f, 1f, 1f)
                    game.font.draw(game.batch, "Waiting...", 390f, 520f)
                } else {
                    game.font.color = Color(0.3f, 1f, 0.3f, 1f)
                    game.font.draw(game.batch, "Player 1 ✅  Player 2 ✅", 390f, 520f)
                    // Weapon row
                    game.font.color = Color.WHITE
                    game.font.draw(game.batch, "Weapon:", 290f, 270f)
                    game.font.draw(game.batch, weapons[myWeapon], 350f, 235f)
                    drawLabel("‹", btnWepL); drawLabel("›", btnWepR)
                    // Map row
                    game.font.draw(game.batch, "Map:", 690f, 270f)
                    game.font.draw(game.batch, maps[selectedMap], 720f, 235f)
                    drawLabel("‹", btnMapL); drawLabel("›", btnMapR)
                    drawLabel("ПОЧАТИ", btnStart)
                }
                drawLabel("← НАЗАД", btnBack)
            }
            NetworkRole.CLIENT -> {
                game.font.color = Color(0.8f, 0.8f, 0.8f, 1f)
                game.font.draw(game.batch, "Enter host IP:", 390f, 450f)
                game.font.color = Color.WHITE
                game.font.draw(game.batch, ipInput.toString() + "|", 400f, 390f)
                if (!connected) {
                    drawLabel("ПІДКЛЮЧИТИСЬ", floatArrayOf(490f, 250f, 300f, 70f))
                } else {
                    game.font.color = Color(0.3f, 1f, 0.3f, 1f)
                    game.font.draw(game.batch, "Connected!", 390f, 200f)
                    game.font.color = Color.WHITE
                    game.font.draw(game.batch, "Weapon:", 290f, 270f)
                    game.font.draw(game.batch, weapons[myWeapon], 350f, 235f)
                    drawLabel("‹", btnWepL); drawLabel("›", btnWepR)
                }
                if (statusMsg.isNotEmpty()) {
                    game.font.color = Color(1f, 0.4f, 0.4f, 1f)
                    game.font.draw(game.batch, statusMsg, 390f, 160f)
                }
                drawLabel("← НАЗАД", btnBack)
            }
        }
    }

    private fun drawLabel(text: String, b: FloatArray) {
        layout.setText(game.font, text)
        game.font.color = Color.WHITE
        game.font.draw(game.batch, text,
            b[0] + b[2] / 2f - layout.width / 2f,
            b[1] + b[3] / 2f + layout.height / 2f)
    }

    private fun handleInput() {
        // Keyboard input for IP
        if (role == NetworkRole.CLIENT && !connected) {
        }

        if (!Gdx.input.justTouched()) return
        val t = viewport.unproject(Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))

        when (role) {
            null -> {
                if (inBtn(t, btnHost)) {
                    role = NetworkRole.HOST
                    networkManager = NetworkManager(NetworkRole.HOST)
                    networkManager?.startHost { connected = true }
                }
                if (inBtn(t, btnJoin))  { role = NetworkRole.CLIENT; showIpKeyboard() }
                if (inBtn(t, btnBack))  { game.setScreen(MenuScreen(game)) }
            }
            NetworkRole.HOST -> {
                if (inBtn(t, btnBack))  { networkManager?.stop(); game.setScreen(MenuScreen(game)) }
                if (connected) {
                    if (inBtn(t, btnWepL)) myWeapon = (myWeapon - 1 + weapons.size) % weapons.size
                    if (inBtn(t, btnWepR)) myWeapon = (myWeapon + 1) % weapons.size
                    if (inBtn(t, btnMapL)) selectedMap = (selectedMap - 1 + maps.size) % maps.size
                    if (inBtn(t, btnMapR)) selectedMap = (selectedMap + 1) % maps.size
                    if (inBtn(t, btnStart)) startGame()
                }
            }
            NetworkRole.CLIENT -> {
                if (inBtn(t, btnBack)) { networkManager?.stop(); role = null }
                if (!connected && inBtn(t, floatArrayOf(490f, 250f, 300f, 70f))) tryConnect()
                if (connected) {
                    if (inBtn(t, btnWepL)) myWeapon = (myWeapon - 1 + weapons.size) % weapons.size
                    if (inBtn(t, btnWepR)) myWeapon = (myWeapon + 1) % weapons.size
                }
            }
        }
    }

    private fun showIpKeyboard() {
        Gdx.input.getTextInput(object : com.badlogic.gdx.Input.TextInputListener {
            override fun input(text: String) { ipInput.clear(); ipInput.append(text) }
            override fun canceled() {}
        }, "IP хоста", ipInput.toString(), "192.168.x.x")
    }

    private fun tryConnect() {
        val ip = ipInput.toString().trim()
        if (ip.isEmpty()) { statusMsg = "Введіть IP!"; return }
        statusMsg = "Connecting..."
        networkManager = NetworkManager(NetworkRole.CLIENT)
        networkManager?.connectToHost(ip,
            onSuccess = { connected = true; statusMsg = "" },
            onError   = { statusMsg = "Connection error" }
        )
    }

    private fun startGame() {
        game.setScreen(GameScreen(game,
            role      = NetworkRole.HOST,
            network   = networkManager!!,
            myWeapon  = myWeapon,
            p2Weapon  = p2Weapon,
            mapIndex  = selectedMap
        ))
    }

    private fun inBtn(p: Vector2, b: FloatArray) =
        p.x >= b[0] && p.x <= b[0] + b[2] && p.y >= b[1] && p.y <= b[1] + b[3]

    override fun resize(width: Int, height: Int) { viewport.update(width, height, true) }
    override fun pause()   {}
    override fun resume()  {}
    override fun hide()    {}
    override fun dispose() { shape.dispose(); networkManager?.stop() }
}
