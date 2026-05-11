package org.example.notifier.application.service.core

import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.domain.position.OpenPositionAssessment
import org.example.notifier.domain.position.OpenPositionRecruiterAccess
interface OpenPositionService {
    suspend fun createPosition(position: OpenPosition, assessmentNames: Map<String, String>): OpenPosition
    suspend fun getPosition(id: String): OpenPosition?
    suspend fun getAllPositions(): List<OpenPosition>
    suspend fun getActivePositions(): List<OpenPosition>
    suspend fun getPositionAssessments(positionId: String): List<OpenPositionAssessment>
    suspend fun updatePosition(
        id: String,
        title: String,
        description: String,
        external: Boolean,
        assessmentIds: List<String>,
        assessmentNames: Map<String, String>,
        fileUrl: String?,
        isFileDeleted: Boolean,
        workMode: String = "Onsite",
        location: String = "",
        jobType: String? = null,
        experienceMin: Int? = null,
        experienceMax: Int? = null,
        skills: List<String> = emptyList()
    ): OpenPosition
    suspend fun updatePositionActiveStatus(id: String, isActive: Boolean): Boolean
    suspend fun grantRecruiterAccess(recruiterId: String, positionId: String, grantedBy: String): OpenPositionRecruiterAccess
    suspend fun revokeRecruiterAccess(recruiterId: String, positionId: String):Boolean
    suspend fun getPositionsByIdsBatch(ids: List<String>): List<OpenPosition>
    suspend fun getRecruiterPositions(recruiterId: String): List<OpenPosition>
    suspend fun getPositionRecruiters(positionId: String): List<OpenPositionRecruiterAccess>
}
