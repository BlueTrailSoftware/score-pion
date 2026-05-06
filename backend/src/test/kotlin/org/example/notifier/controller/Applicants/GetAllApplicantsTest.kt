package org.example.notifier.controller.Applicants

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.shared.PagedResult
import org.example.notifier.application.useCases.getApplicantById.GetApplicantByIdUseCase
import org.example.notifier.application.useCases.getApplicants.GetApplicantsCommand
import org.example.notifier.application.useCases.getApplicants.GetApplicantsUseCase
import org.example.notifier.application.useCases.getPositionById.GetPositionByIdUseCase
import org.example.notifier.application.useCases.getPublicPositions.GetPublicPositionsUseCase
import org.example.notifier.application.useCases.submitApplication.SubmitApplicationUseCase
import org.example.notifier.application.useCases.updateApplicant.UpdateApplicantUseCase
import org.example.notifier.application.useCases.updateApplicantStatus.UpdateApplicantStatusUseCase
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.infrastructure.controller.ApplicantsController
import org.example.notifier.infrastructure.logging.LoggerPort
import org.example.notifier.infrastructure.security.SecurityUtils
import org.example.notifier.infrastructure.util.factory.ResponseEntityFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import java.time.LocalDateTime

class GetAllApplicantsTest {

    private lateinit var submitApplicationUseCase: SubmitApplicationUseCase
    private lateinit var getPublicPositionsUseCase: GetPublicPositionsUseCase
    private lateinit var getPositionByIdUseCase: GetPositionByIdUseCase
    private lateinit var getApplicantsUseCase: GetApplicantsUseCase
    private lateinit var getApplicantByIdUseCase: GetApplicantByIdUseCase
    private lateinit var updateApplicantStatusUseCase: UpdateApplicantStatusUseCase
    private lateinit var updateApplicantUseCase: UpdateApplicantUseCase
    private lateinit var securityUtils: SecurityUtils
    private lateinit var logger: LoggerPort
    private lateinit var controller: ApplicantsController

    private val responseFactory = ResponseEntityFactory()
    private val objectMapper = ObjectMapper().apply { findAndRegisterModules() }
    private val now = LocalDateTime.now()

    private val adminUser = User(
        id = "admin-1",
        email = "admin@example.com",
        name = "Admin",
        role = UserRole.ADMIN,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val recruiterUser = User(
        id = "rec-1",
        email = "recruiter@example.com",
        name = "Recruiter",
        role = UserRole.RECRUITER,
        isActive = true,
        createdAt = now,
        updatedAt = now
    )

    private val applicantItem = ApplicantItem(
        id = "app-1",
        name = "Jane Doe",
        email = "jane@example.com",
        phone = "123456789",
        positionId = "pos-1",
        positionTitle = "Backend Engineer",
        status = "PENDING",
        source = "self_application",
        createdAt = now,
        updatedAt = now,
        reviewedBy = null,
        reviewedAt = null,
        fileUrl = null,
        linkedinUrl = null,
        isFileDeleted = false,
        statusNote = null,
        assessments = null
    )

    @BeforeEach
    fun setup() {
        submitApplicationUseCase = mock(SubmitApplicationUseCase::class.java)
        getPublicPositionsUseCase = mock(GetPublicPositionsUseCase::class.java)
        getPositionByIdUseCase = mock(GetPositionByIdUseCase::class.java)
        getApplicantsUseCase = mock(GetApplicantsUseCase::class.java)
        getApplicantByIdUseCase = mock(GetApplicantByIdUseCase::class.java)
        updateApplicantStatusUseCase = mock(UpdateApplicantStatusUseCase::class.java)
        updateApplicantUseCase = mock(UpdateApplicantUseCase::class.java)
        securityUtils = mock(SecurityUtils::class.java)
        logger = mock(LoggerPort::class.java)

        controller = ApplicantsController(
            submitApplicationUseCase = submitApplicationUseCase,
            getPublicPositionsUseCase = getPublicPositionsUseCase,
            getPositionByIdUseCase = getPositionByIdUseCase,
            getApplicantsUseCase = getApplicantsUseCase,
            getApplicantByIdUseCase = getApplicantByIdUseCase,
            updateApplicantStatusUseCase = updateApplicantStatusUseCase,
            updateApplicantUseCase = updateApplicantUseCase,
            securityUtils = securityUtils,
            responseFactory = responseFactory,
            logger = logger,
            objectMapper = objectMapper
        )
    }

    @Test
    fun `getAllApplicants returns 200 with paged data for admin`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>()))
            .thenReturn(PagedResult(listOf(applicantItem), 1))

        val response = controller.getAllApplicants()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.status)
        assertEquals("Applicants retrieved successfully", response.body?.message)
        assertEquals(1, response.body?.data?.items?.size)
        assertEquals(1, response.body?.data?.total)
    }

    @Test
    fun `getAllApplicants returns 200 with empty list when no applicants found`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>()))
            .thenReturn(PagedResult(emptyList(), 0))

        val response = controller.getAllApplicants()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(0, response.body?.data?.total)
        assertEquals(emptyList<Any>(), response.body?.data?.items)
    }

    @Test
    fun `getAllApplicants passes correct command for admin user`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>()))
            .thenReturn(PagedResult(emptyList(), 0))

        controller.getAllApplicants(status = "PENDING", positionId = "pos-1", search = "jane")

        verify(getApplicantsUseCase).execute(argThat { cmd ->
            cmd.currentUserId == "admin-1" &&
            cmd.isAdmin &&
            cmd.status == "PENDING" &&
            cmd.positionId == "pos-1" &&
            cmd.search == "jane"
        })
    }

    @Test
    fun `getAllApplicants passes isAdmin false for recruiter user`() = runBlocking<Unit> {
        whenever(securityUtils.getCurrentUser()).thenReturn(recruiterUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>()))
            .thenReturn(PagedResult(emptyList(), 0))

        controller.getAllApplicants()

        verify(getApplicantsUseCase).execute(argThat { cmd ->
            cmd.currentUserId == "rec-1" && !cmd.isAdmin
        })
    }

    @Test
    fun `getAllApplicants maps applicant fields to response`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>()))
            .thenReturn(PagedResult(listOf(applicantItem), 1))

        val response = controller.getAllApplicants()

        val data = response.body?.data
        assertNotNull(data)
        with(data!!.items[0]) {
            assertEquals("app-1", id)
            assertEquals("Jane Doe", name)
            assertEquals("jane@example.com", email)
            assertEquals("PENDING", status)
        }
    }

    @Test
    fun `getAllApplicants returns 403 on AccessDeniedException`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(recruiterUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>()))
            .thenThrow(AccessDeniedException("Access denied"))

        val response = controller.getAllApplicants()

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals("error", response.body?.status)
        assertEquals("Access denied to the requested resource", response.body?.message)
    }

    @Test
    fun `getAllApplicants returns 500 on unexpected exception`() = runBlocking {
        whenever(securityUtils.getCurrentUser()).thenReturn(adminUser)
        whenever(getApplicantsUseCase.execute(any<GetApplicantsCommand>())).thenThrow(RuntimeException("DB error"))

        val response = controller.getAllApplicants()

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals("error", response.body?.status)
    }
}
