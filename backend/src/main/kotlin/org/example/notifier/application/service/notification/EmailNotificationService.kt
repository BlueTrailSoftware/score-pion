package org.example.notifier.application.service

import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.notifier.infrastructure.external.EmailTemplate
import org.example.notifier.infrastructure.external.factory.EmailTemplateFactory
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream

private val logger = LoggerFactory.getLogger(EmailNotificationService::class.java)

@Service
class EmailNotificationService(
    private val mailSender: JavaMailSender,
    private val emailTemplateFactory: EmailTemplateFactory,
    @Value("\${app.email.from}") private val defaultFrom: String
) {

    suspend fun sendEmail(template: EmailTemplate): Boolean = withContext(Dispatchers.IO) {
        try {
            val message = createEmail(template)
            mailSender.send(message)
            true
        } catch (e: Exception) {
            logger.error("Error sending email to ${template.to}: ${e.message}", e)
            false
        }
    }

    private fun createEmail(template: EmailTemplate): MimeMessage {
        val message: MimeMessage = mailSender.createMimeMessage()

        val useMultipart = template.hasHtmlContent() || template.hasAttachments()
        val helper = MimeMessageHelper(message, useMultipart, "UTF-8")

        helper.setTo(template.to)
        helper.setFrom(template.from.ifEmpty { defaultFrom })
        helper.setSubject(template.subject)

        if (template.cc.isNotEmpty()) {
            helper.setCc(template.cc.toTypedArray())
        }

        if (template.bcc.isNotEmpty()) {
            helper.setBcc(template.bcc.toTypedArray())
        }

        if (template.hasHtmlContent()) {
            helper.setText( template.textContent, template.htmlContent ?: "")
        } else {
            helper.setText(template.textContent, false)
        }

        if (template.hasAttachments()) {
            template.attachments.forEach { attachment ->
                helper.addAttachment(
                    attachment.filename,
                    { ByteArrayInputStream(attachment.content) },
                    attachment.contentType
                )
            }
        }

        return message
    }
}
