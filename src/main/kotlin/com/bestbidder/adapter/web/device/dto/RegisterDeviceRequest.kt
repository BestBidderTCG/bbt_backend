package com.bestbidder.adapter.web.device.dto

import com.bestbidder.domain.device.DevicePlatform
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterDeviceRequest(
    @field:NotBlank(message = "El token es obligatorio")
    @field:Size(max = 512, message = "El token es demasiado largo")
    val token: String,
    val platform: DevicePlatform,
)
