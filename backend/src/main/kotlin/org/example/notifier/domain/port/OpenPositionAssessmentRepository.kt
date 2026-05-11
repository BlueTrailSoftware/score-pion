package org.example.notifier.domain.port

import org.example.notifier.domain.position.OpenPositionAssessment

interface OpenPositionAssessmentRepository {
    suspend fun save(assessment: OpenPositionAssessment): OpenPositionAssessment
    suspend fun findByPositionId(positionId: String): List<OpenPositionAssessment>
    suspend fun delete(positionId: String, assessmentId: String)
    suspend fun deleteAllByPositionId(positionId: String)
}
