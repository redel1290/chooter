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
import com.shootergame.entities.*
import com.shootergame.network.NetworkManager
import com.shootergame.network.NetworkRole
import com.shootergame.network.PlayerState
import com.shootergame.utils.GameSettings
import com.shootergame.utils.Joystick
import kotlin.math.atan2

class GameScreen(
    private val game: ShooterGame,
    private val role: NetworkRole,
    private val network: NetworkManager,
    myWeapon: Int,
    p2Weapon: Int,
    mapIndex: Int
) : Screen {

    companion object {
        const val WIN_KILLS = 5
        const val SHOOT_COOLDOWN_PISTOL  = 0.25f
        const val SHOOT_COOLDOWN_SHOTGUN = 0.8f
        const val SHOTGUN_PELLETS = 6
        const val SHOTGUN_SPREAD  = 18f
    }

    private val viewport = FitViewport(ShooterGame.VIRTUAL_WIDTH, ShooterGame.VIRTUAL_HEIGHT)
    private val shape    = ShapeRenderer()
    private val layout   = GlyphLayout()

    private val map = GameMap(mapIndex)

    // Local = player 1 if HOST, player 2 if CLIENT
    private val localPlayer = Player(
        x = if (role == NetworkRole.HOST) map.spawn1.first else map.spawn2.first,
        y = if (role == NetworkRole.HOST) map.spawn1.second else map.spawn2.second,
        isLocal = true,
        weaponIndex = myWeapon,
        color = if (role == NetworkRole.HOST) Color(0.2f, 0.5f, 1f, 1f) else Color(1f, 0.3f, 0.3f, 1f)
    )

    private val remotePlayer = Player(
        x = if (role == NetworkRole.HOST) map.spawn2.first else map.spawn1.first,
        y = if (role == NetworkRole.HOST) map.spawn2.second else map.spawn1.second,
        isLocal = false,
        weaponIndex = p2Weapon,
        color = if (role == NetworkRole.HOST) Color(1f, 0.3f, 0.3f, 1f) else Color(0.2f, 0.5f, 1f, 1f)
    )

    private val bullets = mutableListOf<Bullet>()
    private var shootCooldown = 0f

    // Scores
    private var myKills     = 0
    private var remoteKills = 0

    // Joysticks
    private val joySize = 90f * GameSettings.joystickSize
    private val leftJoy  = if (!GameSettings.leftHanded)
        Joystick(140f, 140f, joySize, joySize * 0.45f)
    else
        Joystick(ShooterGame.VIRTUAL_WIDTH - 140f, 140f, joySize, joySize * 0.45f)

    private val rightJoy = if (!GameSettings.leftHanded)
        Joystick(ShooterGame.VIRTUAL_WIDTH - 140f, 140f, joySize, joySize * 0.45f)
    else
        Joystick(140f, 140f, joySize, joySize * 0.45f)

    // Game state
    private var gameOver   = false
    private var winnerMsg  = ""
    private var sendTimer  = 0f
    private val SEND_RATE  = 1f / 20f // 20Hz

    // Flash effect on hit
    private var hitFlash   = 0f

    init {
        network.onStateReceived = { state -> applyRemoteState(state) }
    }

    override fun show() {}

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply()
        val cam = viewport.camera; cam.update()

        if (!gameOver) {
            update(delta)
        }

        shape.projectionMatrix = cam.combined
        shape.begin(ShapeRenderer.ShapeType.Filled)
        draw()
        shape.end()

        game.batch.projectionMatrix = cam.combined
        game.batch.begin()
        drawHUD()
        game.batch.end()

        if (gameOver) drawGameOver()
    }

    private fun update(delta: Float) {
        leftJoy.update(); rightJoy.update()
        shootCooldown -= delta
        hitFlash = (hitFlash - delta).coerceAtLeast(0f)

        // Aim angle from right joystick
        val aimAngle = if (rightJoy.isActive) rightJoy.angle
                       else localPlayer.angle

        // Move local player
        val prevX = localPlayer.x; val prevY = localPlayer.y
        localPlayer.update(delta, leftJoy.dx, leftJoy.dy, aimAngle, rightJoy.isActive)

        // Wall collision
        if (map.collidesWithWall(localPlayer.x, localPlayer.y, Player.RADIUS)) {
            localPlayer.x = prevX; localPlayer.y = prevY
        }

        // Shooting
        if (rightJoy.isActive && shootCooldown <= 0f) {
            shoot()
        }

        // Update bullets
        val toRemove = mutableListOf<Bullet>()
        bullets.forEach { b ->
            b.update(delta)
            if (!b.alive) { toRemove += b; return@forEach }

            // Wall collision
            if (map.bulletHitsWall(b.x, b.y)) { b.alive = false; toRemove += b; return@forEach }

            // Hit remote player (only host is authoritative)
            if (role == NetworkRole.HOST && b.ownerId == 0) {
                if (b.bounds.overlaps(remotePlayer.bounds) && remotePlayer.alive) {
                    remotePlayer.takeDamage(b.damage)
                    b.alive = false; toRemove += b
                    if (!remotePlayer.alive) {
                        myKills++
                        checkWin()
                        scheduleRespawn(remotePlayer, map.spawn2.first, map.spawn2.second)
                    }
                }
            }

            // Hit local player (from remote bullets)
            if (b.ownerId == 1) {
                if (b.bounds.overlaps(localPlayer.bounds) && localPlayer.alive) {
                    localPlayer.takeDamage(b.damage)
                    hitFlash = 0.15f
                    b.alive = false; toRemove += b
                    if (!localPlayer.alive) {
                        remoteKills++
                        checkWin()
                        scheduleRespawn(localPlayer, map.spawn1.first, map.spawn1.second)
                    }
                }
            }
        }
        bullets.removeAll(toRemove)

        // Send state
        sendTimer += delta
        if (sendTimer >= SEND_RATE) {
            sendTimer = 0f
            network.sendState(PlayerState(
                x = localPlayer.x, y = localPlayer.y,
                angle = localPlayer.angle, hp = localPlayer.hp,
                shooting = localPlayer.shooting, weapon = localPlayer.weaponIndex
            ))
        }
    }

    private fun shoot() {
        val cooldown = if (localPlayer.weaponIndex == 0) SHOOT_COOLDOWN_PISTOL
                       else SHOOT_COOLDOWN_SHOTGUN
        shootCooldown = cooldown

        if (localPlayer.weaponIndex == 0) {
            bullets += Bullet(localPlayer.x, localPlayer.y, localPlayer.angle, 0, 0)
        } else {
            for (i in 0 until SHOTGUN_PELLETS) {
                val spread = (Math.random() * SHOTGUN_SPREAD * 2 - SHOTGUN_SPREAD).toFloat()
                bullets += Bullet(localPlayer.x, localPlayer.y, localPlayer.angle + spread, 0, 1)
            }
        }
    }

    private fun applyRemoteState(state: PlayerState) {
        remotePlayer.x     = state.x
        remotePlayer.y     = state.y
        remotePlayer.angle = state.angle
        remotePlayer.hp    = state.hp

        // Spawn remote bullets when remote is shooting
        if (state.shooting && shootCooldown <= 0f) {
            if (remotePlayer.weaponIndex == 0) {
                bullets += Bullet(remotePlayer.x, remotePlayer.y, remotePlayer.angle, 1, 0)
            } else {
                for (i in 0 until SHOTGUN_PELLETS) {
                    val spread = (Math.random() * SHOTGUN_SPREAD * 2 - SHOTGUN_SPREAD).toFloat()
                    bullets += Bullet(remotePlayer.x, remotePlayer.y, remotePlayer.angle + spread, 1, 1)
                }
            }
        }
    }

    private fun scheduleRespawn(player: Player, sx: Float, sy: Float) {
        Thread {
            Thread.sleep(2000)
            Gdx.app.postRunnable { player.respawn(sx, sy) }
        }.also { it.isDaemon = true; it.start() }
    }

    private fun checkWin() {
        if (myKills >= WIN_KILLS) { gameOver = true; winnerMsg = "YOU WIN!" }
        if (remoteKills >= WIN_KILLS) { gameOver = true; winnerMsg = "ENEMY WINS" }
    }

    private fun draw() {
        map.drawFloor(shape)
        map.drawWalls(shape)
        map.drawCrates(shape)

        bullets.forEach { it.draw(shape) }
        if (remotePlayer.alive) remotePlayer.draw(shape)
        if (localPlayer.alive)  localPlayer.draw(shape)

        // Hit flash overlay
        if (hitFlash > 0f) {
            shape.color = Color(1f, 0f, 0f, hitFlash * 0.35f)
            shape.rect(0f, 0f, ShooterGame.VIRTUAL_WIDTH, ShooterGame.VIRTUAL_HEIGHT)
        }

        leftJoy.draw(shape)
        rightJoy.draw(shape)
    }

    private fun drawHUD() {
        game.font.data.setScale(1.5f)
        game.font.color = Color(0.2f, 0.5f, 1f, 1f)
        game.font.draw(game.batch, "Я: $myKills", 20f, ShooterGame.VIRTUAL_HEIGHT - 15f)

        game.font.color = Color(1f, 0.3f, 0.3f, 1f)
        layout.setText(game.font, "Enemy: $remoteKills")
        game.font.draw(game.batch, "Enemy: $remoteKills",
            ShooterGame.VIRTUAL_WIDTH - layout.width - 20f, ShooterGame.VIRTUAL_HEIGHT - 15f)

        // Map name
        game.font.data.setScale(1f)
        game.font.color = Color(0.5f, 0.5f, 0.5f, 0.7f)
        layout.setText(game.font, map.name)
        game.font.draw(game.batch, map.name,
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f, ShooterGame.VIRTUAL_HEIGHT - 15f)

        // Win goal
        game.font.color = Color(0.6f, 0.6f, 0.6f, 0.6f)
        layout.setText(game.font, "до $WIN_KILLS вбивств")
        game.font.draw(game.batch, "до $WIN_KILLS вбивств",
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f, ShooterGame.VIRTUAL_HEIGHT - 35f)

        if (GameSettings.showFps) {
            game.font.color = Color.YELLOW
            game.font.draw(game.batch, "FPS: ${Gdx.graphics.framesPerSecond}", 20f, 30f)
        }
    }

    private fun drawGameOver() {
        shape.projectionMatrix = viewport.camera.combined
        shape.begin(ShapeRenderer.ShapeType.Filled)
        shape.color = Color(0f, 0f, 0f, 0.75f)
        shape.rect(0f, 0f, ShooterGame.VIRTUAL_WIDTH, ShooterGame.VIRTUAL_HEIGHT)
        shape.end()

        game.batch.projectionMatrix = viewport.camera.combined
        game.batch.begin()

        game.font.data.setScale(3.5f)
        val won = winnerMsg == "YOU WIN!"
        game.font.color = if (won) Color(0.2f, 1f, 0.3f, 1f) else Color(1f, 0.3f, 0.2f, 1f)
        layout.setText(game.font, winnerMsg)
        game.font.draw(game.batch, winnerMsg,
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f,
            ShooterGame.VIRTUAL_HEIGHT / 2f + 60f)

        game.font.data.setScale(1.5f)
        game.font.color = Color.WHITE
        val score = "$myKills : $remoteKills"
        layout.setText(game.font, score)
        game.font.draw(game.batch, score,
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f,
            ShooterGame.VIRTUAL_HEIGHT / 2f - 20f)

        game.font.data.setScale(1.3f)
        game.font.color = Color(0.7f, 0.7f, 0.7f, 1f)
        val hint = "Tap anywhere to return"
        layout.setText(game.font, hint)
        game.font.draw(game.batch, hint,
            ShooterGame.VIRTUAL_WIDTH / 2f - layout.width / 2f,
            ShooterGame.VIRTUAL_HEIGHT / 2f - 100f)

        game.batch.end()

        if (Gdx.input.justTouched()) {
            network.stop()
            game.setScreen(MenuScreen(game))
        }
    }

    override fun resize(width: Int, height: Int) { viewport.update(width, height, true) }
    override fun pause()   {}
    override fun resume()  {}
    override fun hide()    {}
    override fun dispose() { shape.dispose(); network.stop() }
}
