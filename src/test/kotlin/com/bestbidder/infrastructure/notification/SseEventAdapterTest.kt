package com.bestbidder.infrastructure.notification

import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.SseEvents
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Executor

class SseEventAdapterTest {
    /** Emitter que guarda lo que se le envía en vez de escribirlo en una respuesta HTTP. */
    private open class RecordingEmitter : SseEmitter() {
        val sent = mutableListOf<String>()

        override fun send(builder: SseEmitter.SseEventBuilder) {
            sent.add(builder.build().joinToString("") { it.data.toString() })
        }

        fun events(): List<String> = sent.filter { it.contains("event:") }
    }

    private class BrokenEmitter : RecordingEmitter() {
        var attempts = 0
        var broken = false

        override fun send(builder: SseEmitter.SseEventBuilder) {
            attempts++
            if (broken) throw IOException("conexión cerrada")
            super.send(builder)
        }
    }

    // Ejecutor síncrono: el envío ocurre en el mismo hilo y se puede comprobar justo después de publicar
    private val adapter = SseEventAdapter(Executor { it.run() })

    private val ana = UUID.randomUUID()
    private val ben = UUID.randomUUID()
    private val auctionId = UUID.randomUUID()

    @Test
    @DisplayName("Al conectar se envía un comentario inicial, no un evento")
    fun `connect sends an initial comment`() {
        val emitter = RecordingEmitter()

        adapter.register(ana, emitter)

        assertEquals(1, emitter.sent.size)
        assertTrue(emitter.events().isEmpty())
    }

    @Test
    @DisplayName("Audience.Everyone llega a todas las conexiones de todos los usuarios")
    fun `everyone reaches all connections`() {
        val anaPhone = RecordingEmitter()
        val anaTablet = RecordingEmitter()
        val benPhone = RecordingEmitter()
        adapter.register(ana, anaPhone)
        adapter.register(ana, anaTablet)
        adapter.register(ben, benPhone)

        adapter.publish(SseEvents.bidPlaced(auctionId, 155.0, "carlos"), Audience.Everyone)

        listOf(anaPhone, anaTablet, benPhone).forEach {
            assertEquals(1, it.events().size)
            assertTrue(it.events().single().contains("event:BID_PLACED"))
        }
    }

    @Test
    @DisplayName("Audience.Users solo llega a las conexiones de ese usuario (todos sus dispositivos)")
    fun `users audience reaches only the target`() {
        val anaPhone = RecordingEmitter()
        val anaTablet = RecordingEmitter()
        val benPhone = RecordingEmitter()
        adapter.register(ana, anaPhone)
        adapter.register(ana, anaTablet)
        adapter.register(ben, benPhone)

        adapter.publish(SseEvents.balanceChanged(auctionId, 200.0), Audience.Users(ana))

        assertEquals(1, anaPhone.events().size)
        assertEquals(1, anaTablet.events().size)
        assertTrue(anaPhone.events().single().contains("event:BALANCE_CHANGED"))
        assertTrue(benPhone.events().isEmpty())
    }

    @Test
    @DisplayName("Publicar a un usuario sin conexiones no falla ni llega a nadie más")
    fun `user without connections is ignored`() {
        val benPhone = RecordingEmitter()
        adapter.register(ben, benPhone)

        adapter.publish(SseEvents.balanceChanged(auctionId, 200.0), Audience.Users(ana))

        assertTrue(benPhone.events().isEmpty())
    }

    @Test
    @DisplayName("Una conexión caída se descarta y el resto sigue recibiendo")
    fun `broken connection is dropped`() {
        val broken = BrokenEmitter()
        val healthy = RecordingEmitter()
        adapter.register(ana, broken)
        adapter.register(ben, healthy)
        broken.broken = true
        val attemptsBefore = broken.attempts

        adapter.publish(SseEvents.auctionStarted(auctionId), Audience.Everyone)
        adapter.publish(SseEvents.auctionStarted(auctionId), Audience.Everyone)

        assertEquals(attemptsBefore + 1, broken.attempts) // solo el primer intento; luego ya no está registrada
        assertEquals(2, healthy.events().size)
    }

    @Test
    @DisplayName("El latido envía un comentario a todas las conexiones")
    fun `heartbeat reaches every connection`() {
        val anaPhone = RecordingEmitter()
        val benPhone = RecordingEmitter()
        adapter.register(ana, anaPhone)
        adapter.register(ben, benPhone)

        adapter.heartbeat()

        assertEquals(2, anaPhone.sent.size) // conexión + latido
        assertEquals(2, benPhone.sent.size)
        assertTrue(anaPhone.events().isEmpty())
    }
}
