package org.example.notifier.controller.AdminUsersController

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getUserProfile.GetUserProfileUseCase
import org.example.notifier.application.model.user.UserProfileResult
import org.example.notifier.application.model.user.ValidateTokenResult
import org.example.notifier.application.useCases.validateToken.ValidateTokenUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.infrastructure.controller.UserController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

class UserControllerTest {

    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var validateTokenUseCase: ValidateTokenUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var responseFactory: ResponseEntityFactory
    private lateinit var userController: UserController

    private val now = LocalDateTime.now()

    private val adminUser = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin User",
        role = "ADMIN",
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val recruiterUser = User(
        id = "rec-1",
        email = "recruiter@example.com",
        name = "Recruiter User",
        role = "RECRUITER",
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private fun User.toProfileResult() = UserProfileResult(
        id = id,
        email = email,
        name = name,
        pictureUrl = pictureUrl,
        role = role,
        isActive = isActive,
        createdAt = createdAt
    )

    @BeforeEach
    fun setup() {
        getUserProfileUseCase = Mockito.mock(GetUserProfileUseCase::class.java)
        validateTokenUseCase = Mockito.mock(ValidateTokenUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)
        responseFactory = ResponseEntityFactory()
        userController = UserController(getUserProfileUseCase, validateTokenUseCase, securityUtils, logger, responseFactory)
    }

    @Test
    fun `getUserProfile should return success when user requests own profile`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(recruiterUser)
        whenever(getUserProfileUseCase.execute(recruiterUser.id)).thenReturn(recruiterUser.toProfileResult())

        val response = userController.getUserProfile(recruiterUser.id)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("User profile retrieved successfully", response.body?.message)
        Assertions.assertEquals(recruiterUser.id, response.body?.data?.id)
    }

    @Test
    fun `getUserProfile should return success when admin requests another user profile`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getUserProfileUseCase.execute(recruiterUser.id)).thenReturn(recruiterUser.toProfileResult())

        val response = userController.getUserProfile(recruiterUser.id)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals(recruiterUser.id, response.body?.data?.id)
    }

    @Test
    fun `getUserProfile should return forbidden when recruiter requests another user profile`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(recruiterUser)

        val response = userController.getUserProfile(adminUser.id)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Access denied", response.body?.message)
    }

    @Test
    fun `getUserProfile should return not found when user does not exist`() = runBlocking {
        val userId = "non-existent-id"
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getUserProfileUseCase.execute(userId)).thenReturn(null)

        val response = userController.getUserProfile(userId)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("User not found", response.body?.message)
    }

    @Test
    fun `validateToken should return success for valid token and active user`() = runBlocking {
        val authHeader = "Bearer valid-token"
        whenever(validateTokenUseCase.execute(authHeader)).thenReturn(ValidateTokenResult(valid = true, message = "Token is valid"))

        val response = userController.validateToken(authHeader)

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("Token is valid", response.body?.message)
    }

    @Test
    fun `validateToken should return unauthorized for invalid token`() = runBlocking {
        val authHeader = "Bearer invalid-token"
        whenever(validateTokenUseCase.execute(authHeader)).thenReturn(ValidateTokenResult(valid = false, message = "Invalid token"))

        val response = userController.validateToken(authHeader)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Invalid token", response.body?.message)
    }
}
