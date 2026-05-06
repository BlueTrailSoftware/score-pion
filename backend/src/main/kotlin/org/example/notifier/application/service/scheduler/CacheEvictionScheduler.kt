package org.example.notifier.application.service.scheduler

import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(CacheEvictionScheduler::class.java)

@Component
class CacheEvictionScheduler {

    @Scheduled(fixedRate = 120000)
    @CacheEvict("assessments", allEntries = true)
    fun evictAssessmentsCache() {
        logger.info("Evicting assessments cache - next fetch will reload from Coderbyte API")
    }
}
