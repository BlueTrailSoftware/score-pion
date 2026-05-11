package org.example.notifier.controller.AdminRecruiters

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.useCases.getRecruiterDetail.GetRecruiterDetailUseCase
import org.example.notifier.application.useCases.getRecruiterPositions.GetRecruiterPositionsUseCase
import org.example.notifier.application.useCases.getRecruiters.GetRecruitersUseCase
import org.example.notifier.application.useCases.inviteUser.InviteUserUseCase
import org.example.notifier.application.useCases.syncRecruiterPositions.SyncRecruiterPositionsUseCase
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusCommand
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusResult
import org.example.notifier.application.useCases.updateRecruiterActiveStatus.UpdateRecruiterActiveStatusUseCase
import org.example.notifier.infrastructure.controller.AdminRecruitersController
import org.example.notifier.infrastructure.dto.request.UpdateActiveStatusRequest
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
import java.util.UUID

class AdminRecruitersControllerUpdateRecruiterActiveStatusTest {

    private lateinit var inviteUserUseCase: InviteUserUseCase
    private lateinit var getRecruitersUseCase: GetRecruitersUseCase
    private lateinit var getRecruiterDetailUseCase: GetRecruiterDetailUseCase
    private lateinit var getRecruiterPositionsUseCase: GetRecruiterPositionsUseCase
    private lateinit var updateRecruiterActiveStatusUseCase: UpdateRecruiterActiveStatusUseCase
    private lateinit var syncRecruiterPositionsUseCase: SyncRecruiterPositionsUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: AdminRecruitersController

    private val responseFactory = ResponseEntityFactory()
    private val now = LocalDateTime.now()
    private val recruiterId = UUID.randomUUID().toString()

    @BeforeEach
    fun setup() {

        inviteUserUseCase = Mockito.mock(InviteUserUseCase::class.java)
        getRecruitersUseCase = Mockito.mock(GetRecruitersUseCase::class.java)
        getRecruiterDetailUseCase = Mockito.mock(GetRecruiterDetailUseCase::class.java)
        getRecruiterPositionsUseCase = Mockito.mock(GetRecruiterPositionsUseCase::class.java)
        updateRecruiterActiveStatusUseCase = Mockito.mock(UpdateRecruiterActiveStatusUseCase::class.java)
        syncRecruiterPositionsUseCase = Mockito.mock(SyncRecruiterPositionsUseCase::class.java)
        securityUtils = Mockito.mock(SecurityUtils::class.java)
        logger = Mockito.mock(LoggerPort::class.java)

        controller = AdminRecruitersController(
            inviteUserUseCase = inviteUserUseCase,
            getRecruitersUseCase = getRecruitersUseCase,
            getRecruiterDetailUseCase = getRecruiterDetailUseCase,
            getRecruiterPositionsUseCase = getRecruiterPositionsUseCase,
            updateRecruiterActiveStatusUseCase = updateRecruiterActiveStatusUseCase,
            syncRecruiterPositionsUseCase = syncRecruiterPositionsUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger
        )
    }

    @Test
    fun `updateRecruiterActiveStatusUseCase should return 200 with success status`()= runBlocking {

        var result = UpdateRecruiterActiveStatusResult(
            id = recruiterId,
            email = "test@test.com",
            name = "test",
            role = "RECRUITER",
            isActive = true,
            createdAt = now
        )

        whenever(updateRecruiterActiveStatusUseCase.execute(
            command = UpdateRecruiterActiveStatusCommand(
                recruiterId = recruiterId, isActive = false,
            )
        )).thenReturn(result)

        val response = controller.updateRecruiterActiveStatus(
            id = recruiterId,
            request = UpdateActiveStatusRequest(
                isActive = false
            )
        )
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("success", response.body?.status)
        Assertions.assertEquals("User status updated successfully", response.body?.message)
    }

    @Test
    fun `updateRecruiterActiveStatus should return 200 with updated data in body`() = runBlocking {

        val result = UpdateRecruiterActiveStatusResult(
            id = recruiterId,
            email = "test@test.com",
            name = "test",
            role = "RECRUITER",
            isActive = true,
            createdAt = now
        )

        whenever(updateRecruiterActiveStatusUseCase.execute(
            command = UpdateRecruiterActiveStatusCommand(recruiterId = recruiterId, isActive = true)
        )).thenReturn(result)

        val response = controller.updateRecruiterActiveStatus(
            id = recruiterId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(recruiterId, response.body?.data?.id)
        Assertions.assertEquals("test@test.com", response.body?.data?.email)
        Assertions.assertEquals("RECRUITER", response.body?.data?.role)
        Assertions.assertEquals(true, response.body?.data?.isActive)
    }

    @Test
    fun `updateRecruiterActiveStatus should return 404 when recruiter is not found`() = runBlocking {

        whenever(updateRecruiterActiveStatusUseCase.execute(
            command = UpdateRecruiterActiveStatusCommand(recruiterId = recruiterId, isActive = false)
        )).thenThrow(IllegalArgumentException("Recruiter not found"))

        val response = controller.updateRecruiterActiveStatus(
            id = recruiterId,
            request = UpdateActiveStatusRequest(isActive = false)
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("Recruiter not found", response.body?.message)
    }

    @Test
    fun `updateRecruiterActiveStatus should return 404 with default message when exception has no message`() = runBlocking {

        whenever(updateRecruiterActiveStatusUseCase.execute(
            command = UpdateRecruiterActiveStatusCommand(recruiterId = recruiterId, isActive = true)
        )).thenThrow(IllegalArgumentException())

        val response = controller.updateRecruiterActiveStatus(
            id = recruiterId,
            request = UpdateActiveStatusRequest(isActive = true)
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("error", response.body?.status)
        Assertions.assertEquals("User not found", response.body?.message)
    }

}