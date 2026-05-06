package org.example.notifier.infrastructure.repository

import org.example.notifier.infrastructure.persistence.DynamoDbAdapter
import org.example.notifier.infrastructure.persistence.DynamoKeyPatterns
import org.example.notifier.infrastructure.persistence.mapper.OpenPositionAssessmentMapper
import org.example.notifier.domain.position.OpenPositionAssessment
import org.springframework.stereotype.Repository

import org.example.notifier.domain.port.OpenPositionAssessmentRepository

@Repository
class DynamoOpenPositionAssessmentRepository(
    private val dynamoDbAdapter: DynamoDbAdapter
) : OpenPositionAssessmentRepository {

    override suspend fun save(assessment: OpenPositionAssessment): OpenPositionAssessment {
        val entity = OpenPositionAssessmentMapper.toDynamoEntity(assessment)
        dynamoDbAdapter.save(entity)
        return assessment
    }

    override suspend fun findByPositionId(positionId: String): List<OpenPositionAssessment> {
        val entities = dynamoDbAdapter.queryByPkStartsWith(
            DynamoKeyPatterns.positionPk(positionId),
            DynamoKeyPatterns.assessmentSkPrefix()
        )
        return entities.map { OpenPositionAssessmentMapper.fromDynamoEntity(it) }
    }

    override suspend fun delete(positionId: String, assessmentId: String) {
        dynamoDbAdapter.delete(
            DynamoKeyPatterns.positionPk(positionId),
            DynamoKeyPatterns.assessmentSk(assessmentId)
        )
    }

    override suspend fun deleteAllByPositionId(positionId: String) {
        val assessments = findByPositionId(positionId)
        assessments.forEach { assessment ->
            delete(positionId, assessment.assessmentId)
        }
    }
}
