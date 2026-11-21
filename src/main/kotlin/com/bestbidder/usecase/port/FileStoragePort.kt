package com.bestbidder.usecase.port

import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface FileStoragePort {
    fun store(
        file: MultipartFile,
        cardId: UUID,
    ): String

    fun delete(cardId: UUID)
}
