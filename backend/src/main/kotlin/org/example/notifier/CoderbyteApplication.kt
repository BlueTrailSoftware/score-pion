package org.example.notifier

import org.example.notifier.infrastructure.logging.LoggerPort
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class NotifierApplication {

    @Bean
    fun startupLogger(logger: LoggerPort): CommandLineRunner {
        return CommandLineRunner {
            logger.info("Application started successfully with clean logging!")
        }
    }
}

fun main(args: Array<String>) {
    runApplication<NotifierApplication>(*args)
}
