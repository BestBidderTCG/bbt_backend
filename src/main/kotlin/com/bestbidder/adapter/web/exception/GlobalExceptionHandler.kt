package com.bestbidder.adapter.web.exception

import com.bestbidder.domain.user.exceptions.InvalidCredentialsException
import com.bestbidder.domain.user.exceptions.InvalidRefreshTokenException
import com.bestbidder.domain.user.exceptions.UserNotFoundException
import com.bestbidder.domain.user.exceptions.UsernameAlreadyExistsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(UsernameAlreadyExistsException::class)
    fun handleConflict(e: UsernameAlreadyExistsException) = build(HttpStatus.CONFLICT, e.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleCredentials(e: InvalidCredentialsException) = build(HttpStatus.UNAUTHORIZED, e.message)

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleRefresh(e: InvalidRefreshTokenException) = build(HttpStatus.UNAUTHORIZED, e.message)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(e: UserNotFoundException) = build(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException) = build(HttpStatus.BAD_REQUEST, e.message ?: "Solicitud inválida")

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val msg = e.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return build(HttpStatus.BAD_REQUEST, msg)
    }

    private fun build(
        status: HttpStatus,
        msg: String?,
    ) = ResponseEntity.status(status).body(
        ErrorResponse(status.value(), status.reasonPhrase, msg ?: "Error"),
    )
}
