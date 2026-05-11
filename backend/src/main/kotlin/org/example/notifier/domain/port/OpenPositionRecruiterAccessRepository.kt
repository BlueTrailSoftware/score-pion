package org.example.notifier.domain.port

import org.example.notifier.domain.position.OpenPositionRecruiterAccess

interface OpenPositionRecruiterAccessRepository {
    suspend fun save(access: OpenPositionRecruiterAccess): OpenPositionRecruiterAccess
    suspend fun findByRecruiterId(recruiterId: String): List<OpenPositionRecruiterAccess>
    suspend fun findByRecruiterIdAndPositionId(recruiterId: String, positionId: String): OpenPositionRecruiterAccess?
    suspend fun findByPositionId(positionId: String): List<OpenPositionRecruiterAccess>
    suspend fun delete(positionId: String, recruiterId: String)
}
