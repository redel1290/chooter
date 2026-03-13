package com.shootergame.utils

import com.badlogic.gdx.Gdx

object GameSettings {
    private val prefs = Gdx.app.getPreferences("shooter_settings")

    // Sound
    var musicVolume: Float
        get() = prefs.getFloat("music_volume", 0.7f)
        set(v) { prefs.putFloat("music_volume", v); prefs.flush() }

    var sfxVolume: Float
        get() = prefs.getFloat("sfx_volume", 1.0f)
        set(v) { prefs.putFloat("sfx_volume", v); prefs.flush() }

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(v) { prefs.putBoolean("sound_enabled", v); prefs.flush() }

    // Network
    var useBluetooth: Boolean
        get() = prefs.getBoolean("use_bluetooth", false)
        set(v) { prefs.putBoolean("use_bluetooth", v); prefs.flush() }

    var networkPort: Int
        get() = prefs.getInteger("network_port", 9876)
        set(v) { prefs.putInteger("network_port", v); prefs.flush() }

    // Controls
    var joystickSize: Float
        get() = prefs.getFloat("joystick_size", 1.0f)
        set(v) { prefs.putFloat("joystick_size", v); prefs.flush() }

    var leftHanded: Boolean
        get() = prefs.getBoolean("left_handed", false)
        set(v) { prefs.putBoolean("left_handed", v); prefs.flush() }

    // Graphics
    var showFps: Boolean
        get() = prefs.getBoolean("show_fps", false)
        set(v) { prefs.putBoolean("show_fps", v); prefs.flush() }

    var renderScale: Float
        get() = prefs.getFloat("render_scale", 1.0f)
        set(v) { prefs.putFloat("render_scale", v); prefs.flush() }
}
