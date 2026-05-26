package org.example.notifier.infrastructure.config

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {

    private val job = SupervisorJob()

    @Bean
    fun applicationScope(): CoroutineScope = CoroutineScope(job + Dispatchers.IO)

    @PreDestroy
    fun onDestroy() {
        job.cancel()
    }
}
