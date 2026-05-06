package org.example.notifier.infrastructure.security

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.security.AuthTokenService
import org.example.notifier.domain.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicReference

class JwtAuthenticationWebFilterTest {

    private lateinit var authTokenService: AuthTokenService
    private lateinit var userService: UserService
    private lateinit var filter: JwtAuthenticationWebFilter

    @BeforeEach
    fun setUp() {
        authTokenService = mock(AuthTokenService::class.java)
        userService = mock(UserService::class.java)
        filter = JwtAuthenticationWebFilter(authTokenService, userService)
    }

    private fun exchangeWithHeader(authHeader: String?): MockServerWebExchange {
        val requestBuilder = MockServerHttpRequest.get("/test")
        if (authHeader != null) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, authHeader)
        }
        return MockServerWebExchange.from(requestBuilder)
    }

    private fun chainCapturingAuth(): Pair<WebFilterChain, AtomicReference<UsernamePasswordAuthenticationToken?>> {
        val captured = AtomicReference<UsernamePasswordAuthenticationToken?>(null)
        val chain = WebFilterChain { _ ->
            ReactiveSecurityContextHolder.getContext()
                .doOnNext { ctx ->
                    captured.set(ctx.authentication as? UsernamePasswordAuthenticationToken)
                }
                .switchIfEmpty(Mono.defer { Mono.empty() })
                .then()
        }
        return chain to captured
    }

    @Test
    fun `no Authorization header continues chain without authentication`() {
        val exchange = exchangeWithHeader(null)
        whenever(authTokenService.extractTokenFromHeader(null)).thenReturn(null)

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        assertNull(captured.get())
    }

    @Test
    fun `invalid token continues chain without authentication`() {
        val exchange = exchangeWithHeader("Bearer bad-token")
        whenever(authTokenService.extractTokenFromHeader("Bearer bad-token")).thenReturn("bad-token")
        whenever(authTokenService.validateToken("bad-token")).thenReturn(false)

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        assertNull(captured.get())
    }

    @Test
    fun `valid token but getUserFromToken throws exception continues without auth`() {
        val exchange = exchangeWithHeader("Bearer valid")
        whenever(authTokenService.extractTokenFromHeader("Bearer valid")).thenReturn("valid")
        whenever(authTokenService.validateToken("valid")).thenReturn(true)

        runBlocking {
            whenever(userService.getUserFromToken("valid")).thenThrow(RuntimeException("DB error"))
        }

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        assertNull(captured.get())
    }

    @Test
    fun `valid token but user not found continues without auth`() {
        val exchange = exchangeWithHeader("Bearer valid")
        whenever(authTokenService.extractTokenFromHeader("Bearer valid")).thenReturn("valid")
        whenever(authTokenService.validateToken("valid")).thenReturn(true)

        runBlocking {
            whenever(userService.getUserFromToken("valid")).thenReturn(null)
        }

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        assertNull(captured.get())
    }

    @Test
    fun `valid token but user inactive continues without auth`() {
        val exchange = exchangeWithHeader("Bearer valid")
        whenever(authTokenService.extractTokenFromHeader("Bearer valid")).thenReturn("valid")
        whenever(authTokenService.validateToken("valid")).thenReturn(true)

        val inactiveUser = User(id = "u1", email = "a@b.com", name = "Test", role = "ADMIN", isActive = false)
        runBlocking {
            whenever(userService.getUserFromToken("valid")).thenReturn(inactiveUser)
        }

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        assertNull(captured.get())
    }

    @Test
    fun `valid token with active ADMIN sets authentication with ROLE_ADMIN`() {
        val exchange = exchangeWithHeader("Bearer valid")
        whenever(authTokenService.extractTokenFromHeader("Bearer valid")).thenReturn("valid")
        whenever(authTokenService.validateToken("valid")).thenReturn(true)

        val adminUser = User(id = "u1", email = "admin@test.com", name = "Admin", role = "ADMIN", isActive = true)
        runBlocking {
            whenever(userService.getUserFromToken("valid")).thenReturn(adminUser)
        }

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        val auth = captured.get()
        assertNotNull(auth)
        assertEquals(adminUser, auth!!.principal)
        assertTrue(auth.authorities.any { it.authority == "ROLE_ADMIN" })
    }

    @Test
    fun `valid token with active RECRUITER sets authentication with ROLE_RECRUITER`() {
        val exchange = exchangeWithHeader("Bearer valid")
        whenever(authTokenService.extractTokenFromHeader("Bearer valid")).thenReturn("valid")
        whenever(authTokenService.validateToken("valid")).thenReturn(true)

        val recruiterUser = User(id = "u2", email = "rec@test.com", name = "Recruiter", role = "RECRUITER", isActive = true)
        runBlocking {
            whenever(userService.getUserFromToken("valid")).thenReturn(recruiterUser)
        }

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        val auth = captured.get()
        assertNotNull(auth)
        assertEquals(recruiterUser, auth!!.principal)
        assertTrue(auth.authorities.any { it.authority == "ROLE_RECRUITER" })
    }

    @Test
    fun `empty Bearer token continues without auth`() {
        val exchange = exchangeWithHeader("Bearer ")
        whenever(authTokenService.extractTokenFromHeader("Bearer ")).thenReturn("")
        whenever(authTokenService.validateToken("")).thenReturn(false)

        val (chain, captured) = chainCapturingAuth()
        filter.filter(exchange, chain).block()

        assertNull(captured.get())
    }
}
