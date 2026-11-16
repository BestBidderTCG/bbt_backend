package com.bestbidder.usecase.port

import org.springframework.web.multipart.MultipartFile

interface FileStoragePort {
    fun store(file: MultipartFile): String
}
