package com.bestbidder.infrastructure.notification

import com.bestbidder.domain.notification.PushMessage
import com.bestbidder.usecase.port.PushNotificationPort
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(name = ["firebase.enabled"], havingValue = "false", matchIfMissing = true)
class NoOpPushNotificationAdapter : PushNotificationPort {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(
        userIds: Collection<UUID>,
        message: PushMessage,
    ) {
        if (userIds.isEmpty()) return
        log.info(
            """
            ================ [PUSH SIMULADA] ================
            Destinatarios : $userIds
            Type          : ${message.type}
            Title         : ${message.title}
            Body          : ${message.body}
            Data          : ${message.data}
            =================================================
            """.trimIndent(),
        )
    }
}
