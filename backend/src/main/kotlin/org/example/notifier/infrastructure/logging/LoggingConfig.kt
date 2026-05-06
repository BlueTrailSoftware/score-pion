package org.example.notifier.infrastructure.logging

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

@Configuration
class LoggingConfig {

    @Bean
    @Scope("prototype")
    fun logger(injectionPoint: InjectionPoint): LoggerPort {
        val classOnWired = injectionPoint.member.declaringClass
        return Slf4jLoggerAdapter(LoggerFactory.getLogger(classOnWired))
    }
}
