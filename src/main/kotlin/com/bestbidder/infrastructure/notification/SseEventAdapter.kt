package com.bestbidder.infrastructure.notification

import com.bestbidder.domain.notification.Audience
import com.bestbidder.domain.notification.SseEvent
import com.bestbidder.usecase.port.RealtimeEventPort
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Canal SSE: mantiene las conexiones abiertas agrupadas por usuario y reparte los eventos
 * según su [Audience].
 *
 * - Espera al commit si hay transacción activa (no se emite algo que acaba en rollback).
 * - Envía desde un único hilo: no bloquea la petición HTTP ni el scheduler, conserva el orden
 *   de los eventos y evita enviar a la vez sobre el mismo emitter (SseEmitter no es thread-safe).
 * - Un latido periódico detecta conexiones muertas y evita que los proxies cierren las inactivas.
 */
@Component
class SseEventAdapter internal constructor(
    private val executor: Executor,
) : RealtimeEventPort {
    @Autowired
    constructor() : this(Executors.newSingleThreadExecutor())

    private companion object {
        const val EMITTER_TIMEOUT_MS = 30L * 60 * 1000
        const val HEARTBEAT_SECONDS = 25L
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val connections = ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>>()

    // Programador propio (no depende de @EnableScheduling, que el proyecto no tiene activado)
    private val heartbeatScheduler = Executors.newSingleThreadScheduledExecutor()

    /**
     * Abre una conexión SSE para [userId]. Un mismo usuario puede tener varias a la vez.
     * Al caducar (30 min) el cliente debe reconectar.
     */
    fun subscribe(userId: UUID): SseEmitter = SseEmitter(EMITTER_TIMEOUT_MS).also { register(userId, it) }

    internal fun register(
        userId: UUID,
        emitter: SseEmitter,
    ) {
        connections.compute(userId) { _, list -> (list ?: CopyOnWriteArrayList()).also { it.add(emitter) } }

        emitter.onCompletion { remove(userId, emitter) }
        emitter.onTimeout { remove(userId, emitter) }
        emitter.onError { remove(userId, emitter) }

        // Comentario inicial: fuerza el envío de cabeceras para que el cliente vea la conexión abierta.
        send(userId, emitter) { SseEmitter.event().comment("connected") }
    }

    override fun publish(
        event: SseEvent,
        audience: Audience,
    ) {
        runAfterCommit { executor.execute { deliver(event, audience) } }
    }

    @PostConstruct
    fun startHeartbeat() {
        heartbeatScheduler.scheduleAtFixedRate({ heartbeat() }, HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS)
    }

    internal fun heartbeat() {
        executor.execute {
            connections.forEach { (userId, emitters) ->
                emitters.forEach { emitter -> send(userId, emitter) { SseEmitter.event().comment("keep-alive") } }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        heartbeatScheduler.shutdown()
        (executor as? ExecutorService)?.shutdown()
    }

    private fun deliver(
        event: SseEvent,
        audience: Audience,
    ) {
        val targets =
            when (audience) {
                Audience.Everyone -> connections.entries.flatMap { (id, list) -> list.map { id to it } }
                is Audience.Users -> audience.userIds.flatMap { id -> connections[id].orEmpty().map { id to it } }
            }

        targets.forEach { (userId, emitter) ->
            send(userId, emitter) {
                SseEmitter.event()
                    .name(event.type.name)
                    .data(event.data, MediaType.APPLICATION_JSON)
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun send(
        userId: UUID,
        emitter: SseEmitter,
        builder: () -> SseEmitter.SseEventBuilder,
    ) {
        try {
            emitter.send(builder())
        } catch (e: Exception) {
            log.debug("Conexión SSE caída para el usuario {}: {}", userId, e.message)
            remove(userId, emitter)
            runCatching { emitter.complete() }
        }
    }

    private fun remove(
        userId: UUID,
        emitter: SseEmitter,
    ) {
        connections.computeIfPresent(userId) { _, list ->
            list.remove(emitter)
            if (list.isEmpty()) null else list
        }
    }

    private fun runAfterCommit(action: () -> Unit) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        action()
                    }
                },
            )
        } else {
            action()
        }
    }
}
