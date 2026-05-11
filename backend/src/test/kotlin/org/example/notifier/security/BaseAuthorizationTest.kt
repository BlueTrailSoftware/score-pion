package org.example.notifier.security

import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.security.AuthTokenService
import org.example.notifier.infrastructure.security.JwtAuthenticationWebFilter
import org.example.notifier.infrastructure.security.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain

/**
 * Base class for authorization integration tests.
 * Mocks the production security beans (JWT filter, token provider, CORS) that
 * get auto-detected by @WebFluxTest so they don't pull in their real dependencies.
 * The JwtAuthenticationWebFilter mock is stubbed to pass-through to the filter chain.
 */
abstract class BaseAuthorizationTest {
    @MockBean private lateinit var jwtAuthenticationWebFilter: JwtAuthenticationWebFilter
    @MockBean private lateinit var jwtTokenProvider: JwtTokenProvider
    @MockBean private lateinit var authTokenService: AuthTokenService
    @MockBean private lateinit var userService: UserService
    @MockBean private lateinit var corsConfigurationSource: CorsConfigurationSource

    @BeforeEach
    fun setupFilterPassThrough() {
        whenever(jwtAuthenticationWebFilter.filter(any<ServerWebExchange>(), any<WebFilterChain>()))
            .thenAnswer { invocation ->
                val exchange = invocation.getArgument<ServerWebExchange>(0)
                val chain = invocation.getArgument<WebFilterChain>(1)
                chain.filter(exchange)
            }
    }
}
