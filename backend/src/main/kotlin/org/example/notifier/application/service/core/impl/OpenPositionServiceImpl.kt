package org.example.notifier.application.service.core.impl

import org.example.notifier.domain.port.OpenPositionRepository
import org.example.notifier.domain.port.OpenPositionAssessmentRepository
import org.example.notifier.domain.port.OpenPositionRecruiterAccessRepository
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.position.OpenPositionRecruiterAccess
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.example.notifier.application.service.core.OpenPositionService
import java.time.LocalDateTime

@Service
class OpenPositionServiceImpl(
    private val positionRepository: OpenPositionRepository,
    private val assessmentRepository: OpenPositionAssessmentRepository,
    private val accessRepository: OpenPositionRecruiterAccessRepository,
) : OpenPositionService {

    private val logger = LoggerFactory.getLogger(OpenPositionServiceImpl::class.java)

    override suspend fun createPosition(position: OpenPosition, assessmentNames: Map<String, String>): OpenPosition {
        val saved = positionRepository.save(position)
        assessmentNames.forEach { (assessmentId, assessmentName) ->
            assessmentRepository.save(
                OpenPositionAssessment(
                    openPositionId = saved.id,
                    assessmentId = assessmentId,
                    assessmentName = assessmentName
                )
            )
        }
        return saved
    }

    /**
     * Gets a position by ID
     */
    override suspend fun getPosition(id: String): OpenPosition? {
        return positionRepository.findById(id)
    }

    /**
     * Gets all positions (active and inactive)
     */
    override suspend fun getAllPositions(): List<OpenPosition> {
        return positionRepository.findAll()
    }

    /**
     * Gets only active positions
     */
    override suspend fun getActivePositions(): List<OpenPosition> {
        return positionRepository.findAllActive()
    }

    /**
     * Gets all assessments for a position
     */
    override suspend fun getPositionAssessments(positionId: String): List<OpenPositionAssessment> {
        return assessmentRepository.findByPositionId(positionId)
    }

    /**
     * Updates a position's title, description and assessments
     */
    override suspend fun updatePosition(
        id: String,
        title: String,
        description: String,
        external: Boolean,
        assessmentIds: List<String>,
        assessmentNames: Map<String, String>,
        fileUrl: String?,
        isFileDeleted: Boolean,
        workMode: String,
        location: String,
        jobType: String?,
        experienceMin: Int?,
        experienceMax: Int?,
        skills: List<String>
    ): OpenPosition {
        val position = positionRepository.findById(id)
            ?: throw IllegalArgumentException("Position not found with id: $id")

        val updatedPosition = position.copy(
            title = title,
            description = description,
            external = external,
            fileUrl = fileUrl,
            isFileDeleted = isFileDeleted,
            workMode = workMode,
            location = location,
            jobType = jobType,
            experienceMin = experienceMin,
            experienceMax = experienceMax,
            skills = skills,
            updatedAt = LocalDateTime.now()
        )

        positionRepository.save(updatedPosition)
        logger.info("Position metadata updated: $id")

        // Sync assessments
        syncAssessments(id, assessmentIds, assessmentNames)

        return updatedPosition
    }

    private suspend fun syncAssessments(
        positionId: String,
        assessmentIds: List<String>,
        assessmentNames: Map<String, String>
    ) {
        val existingAssessments = assessmentRepository.findByPositionId(positionId)
        val existingAssessmentIds = existingAssessments.map { it.assessmentId }.toSet()
        val newAssessmentIds = assessmentIds.toSet()

        // Remove assessments that are no longer in the list
        existingAssessmentIds
            .filter { it !in newAssessmentIds }
            .forEach { assessmentId ->
                assessmentRepository.delete(positionId, assessmentId)
                logger.debug("Removed assessment $assessmentId from position $positionId")
            }

        // Add new assessments
        newAssessmentIds
            .filter { it !in existingAssessmentIds }
            .forEach { assessmentId ->
                val assessment = OpenPositionAssessment(
                    openPositionId = positionId,
                    assessmentId = assessmentId,
                    assessmentName = assessmentNames[assessmentId] ?: "Unknown Assessment"
                )
                assessmentRepository.save(assessment)
                logger.debug("Added assessment $assessmentId to position $positionId")
            }
    }

    /**
     * Updates the active status of a position
     */
    override suspend fun updatePositionActiveStatus(id: String, isActive: Boolean): Boolean {
        val position = positionRepository.findById(id)
            ?: return false

        if (position.isActive == isActive)  {
            logger.warn("Position $id is already ${if (isActive) "active" else "inactive"}")
            return false
        }

        val updatedPosition = position.copy(
            isActive = isActive,
            updatedAt = LocalDateTime.now()
        )

        positionRepository.save(updatedPosition)
        logger.info("Position ${if (isActive) "activated" else "deactivated"}: $id")

        return true
    }

    /**
     * Grants a recruiter access to a position
     */
    override suspend fun grantRecruiterAccess(
        recruiterId: String,
        positionId: String,
        grantedBy: String
    ): OpenPositionRecruiterAccess {
        positionRepository.findById(positionId)
            ?: throw IllegalArgumentException("Position not found with id: $positionId")

        // Check if access already exists
        val existingAccess = accessRepository.findByRecruiterIdAndPositionId(recruiterId, positionId)
        if (existingAccess != null)  {
            if (existingAccess.isActive) {
                logger.warn("Recruiter $recruiterId already has active access to position $positionId")
                return existingAccess
            } else {
                // Reactivate the access
                val reactivatedAccess = existingAccess.copy(
                    isActive = true,
                    grantedBy = grantedBy
                )
                return accessRepository.save(reactivatedAccess)
            }
        }

        val access = OpenPositionRecruiterAccess(
            openPositionId = positionId,
            recruiterId = recruiterId,
            grantedBy = grantedBy
        )

        val savedAccess = accessRepository.save(access)
        logger.info("Granted recruiter $recruiterId access to position $positionId")

        return savedAccess
    }

    /**
     * Revokes a recruiter's access to a position
     */
    override suspend fun revokeRecruiterAccess(recruiterId: String, positionId: String): Boolean {
        val access = accessRepository.findByRecruiterIdAndPositionId(recruiterId, positionId)
            ?: return false

        if (!access.isActive) {
            logger.warn("Access already revoked for recruiter $recruiterId on position $positionId")
            return false
        }

        val revokedAccess = access.copy(isActive = false)
        accessRepository.save(revokedAccess)
        logger.info("Revoked recruiter $recruiterId access to position $positionId")

        return true
    }

    /**
     * Gets multiple positions by their IDs
     */
    override suspend fun getPositionsByIdsBatch(ids: List<String>): List<OpenPosition> {
        if (ids.isEmpty()) return emptyList()

        logger.debug("Fetching ${ids.size} positions by IDs (batch)")
        return positionRepository.findByIdsBatch(ids)
    }

    /**
     * Gets all positions accessible by a recruiter
     */
    override suspend fun getRecruiterPositions(recruiterId: String): List<OpenPosition> {
        val accesses = accessRepository.findByRecruiterId(recruiterId)
            .filter { it.isActive }

        return accesses.mapNotNull { access ->
            positionRepository.findById(access.openPositionId)
        }.filter { it.isActive }
    }

    /**
     * Gets all recruiters with access to a position
     */
    override suspend fun getPositionRecruiters(positionId: String): List<OpenPositionRecruiterAccess> {
        return accessRepository.findByPositionId(positionId)
            .filter { it.isActive }
    }


}
