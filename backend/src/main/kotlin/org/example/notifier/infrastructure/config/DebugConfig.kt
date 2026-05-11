package org.example.notifier.infrastructure.config

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import java.io.InputStream
import java.util.Properties

private val logger = LoggerFactory.getLogger("DebugMailSender")

/**
 * Registers no-op infrastructure beans when app.debug.enabled=true.
 * Spring Boot's MailSenderAutoConfiguration skips its own bean because
 * @ConditionalOnMissingBean(MailSender) is already satisfied by this one.
 */
@Configuration
@ConditionalOnProperty(name = ["app.debug.enabled"], havingValue = "true")
class DebugConfig {

    @Bean
    fun debugMailSender(): JavaMailSender = object : JavaMailSender {

        private val session: Session = Session.getDefaultInstance(Properties())

        override fun createMimeMessage(): MimeMessage = MimeMessage(session)

        override fun createMimeMessage(contentStream: InputStream): MimeMessage = MimeMessage(session)

        override fun send(mimeMessage: MimeMessage) {
            logger.info("[DEBUG] Skipping email send (MimeMessage)")
        }

        override fun send(vararg mimeMessages: MimeMessage) {
            logger.info("[DEBUG] Skipping batch email send ({} messages)", mimeMessages.size)
        }

        override fun send(simpleMessage: SimpleMailMessage) {
            logger.info("[DEBUG] Skipping email send to: {}", simpleMessage.to?.joinToString())
        }

        override fun send(vararg simpleMessages: SimpleMailMessage) {
            logger.info("[DEBUG] Skipping batch email send ({} messages)", simpleMessages.size)
        }
    }
}