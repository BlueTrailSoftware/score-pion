package org.example.notifier.infrastructure.util.factory

import org.example.notifier.infrastructure.dto.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ResponseEntityFactory {

    // Create success response (HTTP 200)
    fun <T> success(
        message: String,
        data: T? = null
    ): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.ok(
            ApiResponse(
                status = "success",
                message = message,
                data = data
            )
        )
    }

    // Create error response
    fun <T> error(
        message: String,
        status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    ): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.status(status).body(
            ApiResponse(
                status = "error",
                message = message
            )
        )
    }

    // Create not found response (HTTP 404)
    fun <T> notFound(message: String): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.NOT_FOUND)
    }

    // Create bad request response (HTTP 400)
    fun <T> badRequest(message: String): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.BAD_REQUEST)
    }

    // Create forbidden response (HTTP 403)
    fun <T> forbidden(message: String): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.FORBIDDEN)
    }

    // Create unauthorized response (HTTP 401)
    fun <T> unauthorized(message: String): ResponseEntity<ApiResponse<T>> {
        return error(message, HttpStatus.UNAUTHORIZED)
    }

    // Create no content response (HTTP 204)
    fun <T> noContent(): ResponseEntity<ApiResponse<T>> {
        return ResponseEntity.noContent().build()
    }
}
