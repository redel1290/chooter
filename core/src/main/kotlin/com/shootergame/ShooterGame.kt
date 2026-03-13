package com.shootergame

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.shootergame.screens.MenuScreen
import com.shootergame.utils.Assets

class ShooterGame : Game() {

    lateinit var batch: SpriteBatch
    lateinit var font: BitmapFont
    lateinit var assets: Assets

    companion object {
        const val VERSION = "v1.0.0"
        const val VIRTUAL_WIDTH  = 1280f
        const val VIRTUAL_HEIGHT = 720f
    }

    override fun create() {
        batch  = SpriteBatch()
        font   = BitmapFont()
        assets = Assets()
        assets.load()
        setScreen(MenuScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        assets.dispose()
        screen?.dispose()
    }
}
