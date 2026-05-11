package org.example.notifier.security

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

@TestConfiguration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
class TestSecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().authenticated() }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    val body = """{"error": "Unauthorized", "message": "Authentication required"}"""
                    exchange.response.writeWith(
                        Mono.just(exchange.response.bufferFactory().wrap(body.toByteArray()))
                    )
                }
                .accessDeniedHandler { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    val body = """{"error": "Forbidden", "message": "Access denied"}"""
                    exchange.response.writeWith(
                        Mono.just(exchange.response.bufferFactory().wrap(body.toByteArray()))
                    )
                }
            }
            .build()
    }
}
