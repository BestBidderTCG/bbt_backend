package com.bestbidder.infrastructure.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

/**
 * Inicializa Firebase Admin SDK. Solo se carga con `firebase.enabled=true`;
 * sin eso la app arranca igual y las push se ignoran (ver NoOpPushNotificationAdapter).
 *
 * Credenciales (cuenta de servicio de Firebase), una de las dos:
 *  - FIREBASE_CREDENTIALS_PATH: ruta al JSON (desarrollo local)
 *  - FIREBASE_CREDENTIALS_JSON: contenido del JSON (despliegue, sin subir ficheros)
 */
@Configuration
@ConditionalOnProperty(name = ["firebase.enabled"], havingValue = "true")
class FirebaseConfig(
    @Value("\${firebase.credentials-path:}") private val credentialsPath: String,
    @Value("\${firebase.credentials-json:}") private val credentialsJson: String,
) {
    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val credentials =
            when {
                credentialsJson.isNotBlank() ->
                    GoogleCredentials.fromStream(credentialsJson.byteInputStream())
                credentialsPath.isNotBlank() ->
                    FileInputStream(credentialsPath).use { GoogleCredentials.fromStream(it) }
                else ->
                    error("firebase.enabled=true pero falta FIREBASE_CREDENTIALS_PATH o FIREBASE_CREDENTIALS_JSON")
            }

        val app =
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(credentials).build())
            } else {
                FirebaseApp.getInstance()
            }
        return FirebaseMessaging.getInstance(app)
    }
}
