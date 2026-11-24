package com.bestbidder.infrastructure.notification

import com.bestbidder.domain.notification.PushMessage
import com.bestbidder.usecase.port.DeviceTokenRepository
import com.bestbidder.usecase.port.PushNotificationPort
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Envía las push a través de Firebase Cloud Messaging.
 *
 * - Espera al commit si hay transacción activa (no se notifica algo que acaba en rollback).
 * - Envía en un hilo aparte para no bloquear la petición HTTP ni el scheduler.
 * - Borra los tokens que FCM marca como no registrados (app desinstalada, token caducado).
 */
@Component
@ConditionalOnProperty(name = ["firebase.enabled"], havingValue = "true")
class FcmPushNotificationAdapter(
    private val firebaseMessaging: FirebaseMessaging,
    private val deviceTokenRepository: DeviceTokenRepository,
) : PushNotificationPort {
    private companion object {
        const val MAX_TOKENS_PER_BATCH = 500
        const val POOL_SIZE = 2
        const val TYPE_KEY = "type"
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val executor = Executors.newFixedThreadPool(POOL_SIZE)

    override fun send(
        userIds: Collection<UUID>,
        message: PushMessage,
    ) {
        if (userIds.isEmpty()) return
        val recipients = userIds.toSet()
        runAfterCommit { executor.execute { deliver(recipients, message) } }
    }

    @PreDestroy
    fun shutdown() {
        executor.shutdown()
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

    @Suppress("TooGenericExceptionCaught")
    private fun deliver(
        userIds: Set<UUID>,
        message: PushMessage,
    ) {
        try {
            val tokens = deviceTokenRepository.findTokensByUserIds(userIds)
            log.info(
                "\n=== [PUSH] ===\nPara: {}\nTipo: {}\nTítulo: {}\nCuerpo: {}\nData: {}\n==============",
                userIds,
                message.type,
                message.title,
                message.body,
                message.data,
            )
            tokens.chunked(MAX_TOKENS_PER_BATCH).forEach { batch -> sendBatch(batch, message) }
        } catch (e: Exception) {
            log.error("No se pudo enviar la push {}", message.type, e)
        }
    }

    private fun sendBatch(
        tokens: List<String>,
        message: PushMessage,
    ) {
        val multicast =
            MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(
                    Notification.builder().setTitle(message.title).setBody(message.body).build(),
                )
                .setAndroidConfig(
                    AndroidConfig.builder().setPriority(AndroidConfig.Priority.HIGH).build(),
                )
                .putAllData(message.data + (TYPE_KEY to message.type.name))
                .build()

        val response = firebaseMessaging.sendEachForMulticast(multicast)
        response.responses.forEachIndexed { index, result ->
            if (!result.isSuccessful) handleFailure(tokens[index], result.exception)
        }
    }

    private fun handleFailure(
        token: String,
        error: FirebaseMessagingException?,
    ) {
        if (error?.messagingErrorCode == MessagingErrorCode.UNREGISTERED) {
            deviceTokenRepository.removeByToken(token)
        } else {
            log.warn("FCM rechazó un envío: {}", error?.message)
        }
    }
}
