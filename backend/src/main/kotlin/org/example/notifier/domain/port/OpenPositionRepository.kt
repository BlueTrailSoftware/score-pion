package org.example.notifier.domain.port

import org.example.notifier.domain.position.OpenPosition

interface OpenPositionRepository {
    suspend fun save(position: OpenPosition): OpenPosition
    suspend fun findByIdsBatch(ids: List<String>): List<OpenPosition>
    suspend fun findById(id: String): OpenPosition?
    suspend fun findAll(): List<OpenPosition>
    suspend fun findAllActive(): List<OpenPosition>
    suspend fun delete(position: OpenPosition)
}
