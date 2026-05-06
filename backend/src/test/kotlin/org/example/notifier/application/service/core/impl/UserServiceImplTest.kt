package org.example.notifier.application.service.core.impl

import kotlinx.coroutines.runBlocking
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.example.notifier.application.service.security.AuthTokenService
import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.user.User
import org.example.notifier.domain.user.UserRole
import org.example.notifier.domain.port.UserRepository
import org.example.notifier.infrastructure.external.Auth0UserInfo
import org.example.notifier.infrastructure.external.GoogleUserInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

class UserServiceImplTest {

    private lateinit var userRepository: UserRepository
    private lateinit var authTokenService: AuthTokenService
    private lateinit var recruiterInvitationService: RecruiterInvitationService
    private lateinit var applicationEventPublisher: ApplicationEventPublisher
    private lateinit var service: UserServiceImpl
    private val adminEmail = "admin@example.com"

    @BeforeEach
    fun setup() {
        userRepository = mock(UserRepository::class.java)
        authTokenService = mock(AuthTokenService::class.java)
        recruiterInvitationService = mock(RecruiterInvitationService::class.java)
        applicationEventPublisher = mock(ApplicationEventPublisher::class.java)
        service = UserServiceImpl(
            userRepository,
            authTokenService,
            recruiterInvitationService,
            applicationEventPublisher,
            adminEmail
        )
    }

    // --- findById ---

    @Test
    fun `findById returns user when found`() = runBlocking<Unit> {
        val user = User(id = "u-1", email = "user@example.com", name = "Alice", role = UserRole.RECRUITER)
        whenever(userRepository.findById("u-1")).thenReturn(user)

        val result = service.findById("u-1")

        assertEquals(user, result)
    }

    @Test
    fun `findById returns null when user does not exist`() = runBlocking<Unit> {
        whenever(userRepository.findById("unknown")).thenReturn(null)

        val result = service.findById("unknown")

        assertNull(result)
    }

    // --- createOrUpdateFromGoogle ---

    @Test
    fun `createOrUpdateFromGoogle should use role from invitation for new admin`() = runBlocking<Unit> {
        val googleUserInfo = GoogleUserInfo(
            googleId = "google-123",
            email = "newadmin@example.com",
            name = "New Admin",
            pictureUrl = "http://pic.com",
            emailVerified = true
        )
        val invitation = RecruiterInvitation(
            email = "newadmin@example.com",
            invitedBy = "admin-1",
            role = UserRole.ADMIN
        )

        whenever(userRepository.findByGoogleId(any())).thenReturn(null)
        whenever(userRepository.findByEmail(any())).thenReturn(null)
        whenever(recruiterInvitationService.findByEmail(googleUserInfo.email)).thenReturn(invitation)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = service.createOrUpdateFromGoogle(googleUserInfo)

        assert(result != null)
        assert(result?.role == UserRole.ADMIN)
        verify(userRepository).save(argThat { this.role == UserRole.ADMIN })
        verify(recruiterInvitationService).acceptInvitation(invitation)
    }

    @Test
    fun `findAllByRole should delegate to repository`() = runBlocking<Unit> {
        val recruiters = listOf(
            User(id = "r-1", email = "a@example.com", name = "Alice", role = UserRole.RECRUITER),
            User(id = "r-2", email = "b@example.com", name = "Bob", role = UserRole.RECRUITER)
        )
        whenever(userRepository.findAllByRole(UserRole.RECRUITER)).thenReturn(recruiters)

        val result = service.findAllByRole(UserRole.RECRUITER)

        assert(result == recruiters)
        verify(userRepository).findAllByRole(UserRole.RECRUITER)
    }

    @Test
    fun `findAllByRole should return empty list when no users match role`() = runBlocking<Unit> {
        whenever(userRepository.findAllByRole(UserRole.RECRUITER)).thenReturn(emptyList())

        val result = service.findAllByRole(UserRole.RECRUITER)

        assert(result.isEmpty())
    }

    @Test
    fun `createOrUpdateFromAuth0 should use role from invitation for new admin`() = runBlocking<Unit> {
        val auth0UserInfo = Auth0UserInfo(
            auth0Id = "auth0|123",
            email = "newadmin@example.com",
            name = "New Admin",
            pictureUrl = "http://pic.com",
            emailVerified = true
        )
        val invitation = RecruiterInvitation(
            email = "newadmin@example.com",
            invitedBy = "admin-1",
            role = UserRole.ADMIN
        )

        whenever(userRepository.findByEmail(any())).thenReturn(null)
        whenever(recruiterInvitationService.findByEmail(auth0UserInfo.email)).thenReturn(invitation)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = service.createOrUpdateFromAuth0(auth0UserInfo)

        assert(result != null)
        assert(result?.role == UserRole.ADMIN)
        verify(userRepository).save(argThat { this.role == UserRole.ADMIN })
        verify(recruiterInvitationService).acceptInvitation(invitation)
    }
}