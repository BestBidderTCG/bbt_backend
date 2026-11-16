package com.bestbidder.infrastructure.storage

import com.bestbidder.usecase.port.FileStoragePort
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Component
class LocalFileStorageAdapter : FileStoragePort {
    private val uploadDir = Paths.get("uploads")

    init {
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir)
    }

    override fun store(file: MultipartFile): String {
        val filename = "${UUID.randomUUID()}_${file.originalFilename}"
        val targetPath = uploadDir.resolve(filename)
        Files.copy(file.inputStream, targetPath)
        return "/uploads/$filename" // La url puede variar por tema de la carta
    }
}