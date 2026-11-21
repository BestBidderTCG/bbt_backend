package com.bestbidder.infrastructure.storage

import com.bestbidder.usecase.port.FileStoragePort
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Component
@Primary
class CloudinaryFileStorageAdapter(
    @Value("\${cloudinary.cloud-name}") cloudName: String,
    @Value("\${cloudinary.api-key}") apiKey: String,
    @Value("\${cloudinary.api-secret}") apiSecret: String,
) : FileStoragePort {
    private val cloudinary =
        Cloudinary(
            mapOf(
                "cloud_name" to cloudName,
                "api_key" to apiKey,
                "api_secret" to apiSecret,
                "secure" to true,
            ),
        )

    override fun store(
        file: MultipartFile,
        cardId: UUID,
    ): String {
        val uploadResult =
            cloudinary.uploader().upload(
                file.bytes,
                ObjectUtils.asMap(
                    "folder",
                    "bestbidder",
                    "public_id",
                    cardId.toString(),
                    "overwrite",
                    true,
                    "resource_type",
                    "image",
                ),
            )
        return uploadResult["secure_url"].toString()
    }

    override fun delete(cardId: UUID) {
        val publicId = "bestbidder/$cardId"
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
    }
}
