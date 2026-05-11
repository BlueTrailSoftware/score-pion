package org.example.notifier.infrastructure.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.reactive.CorsConfigurationSource
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationWebFilter: JwtAuthenticationWebFilter,
    private val corsConfigurationSource: CorsConfigurationSource,
    @Value("\${springdoc.swagger-ui.enabled:false}") private val swaggerEnabled: Boolean
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource) }
            .authorizeExchange { auth ->
                if (swaggerEnabled) {
                    auth.pathMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/webjars/**"
                    ).permitAll()
                }
                auth.pathMatchers(
                        "/google-sso",
                        "/auth0-login",
                        "/auth0-callback",
                        "/auth0-logout",
                        "/auth0/check-invitation",
                        "/actuator/info",
                        "/webhook/coderbyte",
                        "/webhook/coderbyte/assessment/joined",
                        "/webhook/coderbyte/assessment/completed",
                        "/applicants/apply",
                        "/applicants/positions",
                        "/applicants/positions/**",
                        "/applicants/privacy/**"
                    ).permitAll()
                    // Protected endpoints
                    .pathMatchers(
                        "/users/**",
                        "/assessments",
                        "/assessments/**"
                    ).authenticated()
                    .anyExchange().authenticated()
            }
            .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .exceptionHandling { exceptions ->
                exceptions.authenticationEntryPoint { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    val body = """{"error": "Unauthorized", "message": "Authentication required"}"""
                    exchange.response.writeWith(
                        Mono.just(
                        exchange.response.bufferFactory().wrap(body.toByteArray())
                    ))
                }
                .accessDeniedHandler { exchange, _ ->
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                    exchange.response.headers.contentType = MediaType.APPLICATION_JSON
                    val body = """{"error": "Forbidden", "message": "Access denied"}"""
                    exchange.response.writeWith(Mono.just(
                        exchange.response.bufferFactory().wrap(body.toByteArray())
                    ))
                }
            }
            .build()
    }
}
