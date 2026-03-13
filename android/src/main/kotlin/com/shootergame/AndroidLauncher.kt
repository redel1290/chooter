package com.shootergame

import android.os.Bundle
import android.widget.Toast
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val config = AndroidApplicationConfiguration().apply {
                useImmersiveMode = true
                useWakelock = true
            }
            initialize(ShooterGame(), config)
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка: ${e.message} / ${e.cause}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
