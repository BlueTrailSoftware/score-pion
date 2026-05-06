package org.example.notifier.infrastructure.security

import kotlinx.coroutines.reactor.mono
import org.example.notifier.application.service.core.UserService
import org.example.notifier.application.service.security.AuthTokenService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
@Component
class JwtAuthenticationWebFilter(
    private val authTokenService: AuthTokenService,
    private val userService: UserService
) : WebFilter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationWebFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val authHeader = exchange.request.headers.getFirst("Authorization")
        val token = authTokenService.extractTokenFromHeader(authHeader)

        if (token == null || !authTokenService.validateToken(token)) {
            return chain.filter(exchange)
        }

        return mono {
            try {
                userService.getUserFromToken(token)
            } catch (e: Exception) {
                logger.error("Error getting user from token: ${e.message}", e)
                null
            }
        }.flatMap { user ->
            if (user != null && user.isActive) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role}"))
                val authentication = UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    authorities
                )

                logger.debug("Authentication successful for user: ${user.email}")

                chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            } else {
                logger.debug("User not found or inactive")
                chain.filter(exchange)
            }
        }
    }
}
