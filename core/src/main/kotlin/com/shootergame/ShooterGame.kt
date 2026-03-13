package com.shootergame

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.Texture
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
        assets = Assets()
        assets.load()
        font = BitmapFont()
        font.data.setScale(2f)
        font.region.texture.setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        )
        setScreen(MenuScreen(this))
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        assets.dispose()
        screen?.dispose()
    }
}
