package org.example.notifier.application.service.core.impl

import org.example.notifier.domain.invitation.RecruiterInvitation
import org.example.notifier.domain.port.RecruiterInvitationRepository
import org.example.notifier.application.service.core.RecruiterInvitationService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RecruiterInvitationServiceImpl(
    private val recruiterInvitationRepository: RecruiterInvitationRepository
) : RecruiterInvitationService {

    override suspend fun createInvitation(email: String, invitedBy: String, positionIds: List<String>?, role: String?): RecruiterInvitation {
        val existing = recruiterInvitationRepository.findByEmail(email)
        if (existing != null)  {
            throw IllegalArgumentException("An invitation for this email already exists")
        }

        val invitation = RecruiterInvitation(
            email = email,
            invitedBy = invitedBy,
            assignedPositions = positionIds ?: emptyList(),
            role = role ?: org.example.notifier.domain.user.UserRole.RECRUITER
        )

        return recruiterInvitationRepository.save(invitation)
    }

    override suspend fun findByEmail(email: String): RecruiterInvitation? {
        return recruiterInvitationRepository.findByEmail(email)
    }

    override suspend fun acceptInvitation(invitation: RecruiterInvitation): RecruiterInvitation {
        val updatedInvitation = invitation.copy(
            status = RecruiterInvitation.InvitationStatus.ACCEPTED,
            acceptedAt = LocalDateTime.now()
        )
        return recruiterInvitationRepository.save(updatedInvitation)
    }

    override suspend fun revokeInvitation(email: String): Boolean {
        val invitation = findByEmail(email) ?: return false

        if (invitation.status == RecruiterInvitation.InvitationStatus.ACCEPTED)  {
            return false
        }

        val revokedInvitation = invitation.copy(
            status = RecruiterInvitation.InvitationStatus.REVOKED
        )
        recruiterInvitationRepository.save(revokedInvitation)

        return true
    }

    override suspend fun getAllPendingInvitations(): List<RecruiterInvitation> {
        return recruiterInvitationRepository.findAll()
            .filter { it.status == RecruiterInvitation.InvitationStatus.PENDING && it.isValid() }
    }

    override suspend fun getAllInvitations(): List<RecruiterInvitation> {
        return recruiterInvitationRepository.findAll()
    }
}
