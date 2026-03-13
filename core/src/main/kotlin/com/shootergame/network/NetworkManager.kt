package com.shootergame.network

import com.badlogic.gdx.Gdx
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.concurrent.atomic.AtomicBoolean

enum class NetworkRole { HOST, CLIENT }

data class PlayerState(
    val x: Float       = 0f,
    val y: Float       = 0f,
    val angle: Float   = 0f,
    val hp: Int        = 100,
    val shooting: Boolean = false,
    val weapon: Int    = 0
)

class NetworkManager(private val role: NetworkRole) {

    companion object {
        const val PORT       = 9876
        const val PACKET_HZ  = 20   // 50ms
        const val BUFFER_SIZE = 128
    }

    private var socket: DatagramSocket? = null
    private var remoteAddress: InetAddress? = null
    private var running = AtomicBoolean(false)
    private var receiveThread: Thread? = null

    var localIp: String = getLocalIp()
    var onStateReceived: ((PlayerState) -> Unit)? = null
    var onConnected:     (() -> Unit)?             = null

    // Latest received state from remote player
    var remoteState = PlayerState()
        private set

    fun startHost(onClientConnected: () -> Unit) {
        socket = DatagramSocket(PORT)
        running.set(true)
        receiveThread = Thread {
            val buf = ByteArray(BUFFER_SIZE)
            while (running.get()) {
                try {
                    val packet = DatagramPacket(buf, buf.size)
                    socket?.receive(packet)
                    if (remoteAddress == null) {
                        remoteAddress = packet.address
                        Gdx.app.postRunnable { onClientConnected() }
                    }
                    remoteState = decode(packet.data)
                    Gdx.app.postRunnable { onStateReceived?.invoke(remoteState) }
                } catch (_: Exception) {}
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun connectToHost(ip: String, onSuccess: () -> Unit, onError: () -> Unit) {
        Thread {
            try {
                remoteAddress = InetAddress.getByName(ip)
                socket = DatagramSocket()
                // Send handshake
                val handshake = encode(PlayerState())
                val packet = DatagramPacket(handshake, handshake.size, remoteAddress, PORT)
                socket?.send(packet)
                running.set(true)
                Gdx.app.postRunnable { onSuccess() }
                // Start receive loop
                receiveThread = Thread {
                    val buf = ByteArray(BUFFER_SIZE)
                    while (running.get()) {
                        try {
                            val p = DatagramPacket(buf, buf.size)
                            socket?.receive(p)
                            remoteState = decode(p.data)
                            Gdx.app.postRunnable { onStateReceived?.invoke(remoteState) }
                        } catch (_: Exception) {}
                    }
                }.also { it.isDaemon = true; it.start() }
            } catch (e: Exception) {
                Gdx.app.postRunnable { onError() }
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun sendState(state: PlayerState) {
        val addr = remoteAddress ?: return
        Thread {
            try {
                val data = encode(state)
                val packet = DatagramPacket(data, data.size, addr,
                    if (role == NetworkRole.HOST) socket!!.localPort else PORT)
                socket?.send(packet)
            } catch (_: Exception) {}
        }.also { it.isDaemon = true; it.start() }
    }

    fun update() {
        // Called each frame — can be used for tick-rate limiting later
    }

    fun stop() {
        running.set(false)
        socket?.close()
        receiveThread?.interrupt()
    }

    // Simple binary encoding: x(4) y(4) angle(4) hp(4) shooting(1) weapon(1) = 18 bytes
    private fun encode(s: PlayerState): ByteArray {
        val buf = java.nio.ByteBuffer.allocate(BUFFER_SIZE)
        buf.putFloat(s.x)
        buf.putFloat(s.y)
        buf.putFloat(s.angle)
        buf.putInt(s.hp)
        buf.put(if (s.shooting) 1 else 0)
        buf.put(s.weapon.toByte())
        return buf.array()
    }

    private fun decode(data: ByteArray): PlayerState {
        val buf = java.nio.ByteBuffer.wrap(data)
        return PlayerState(
            x        = buf.float,
            y        = buf.float,
            angle    = buf.float,
            hp       = buf.int,
            shooting = buf.get() == 1.toByte(),
            weapon   = buf.get().toInt()
        )
    }

    private fun getLocalIp(): String {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .firstOrNull { !it.isLoopbackAddress && it.hostAddress.contains('.') }
                ?.hostAddress ?: "Unknown"
        } catch (_: Exception) { "Unknown" }
    }
}
