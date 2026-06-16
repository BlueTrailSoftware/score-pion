package org.example.notifier.infrastructure.external.factory

import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.shared.EmailEvent
import org.example.notifier.domain.shared.AssessmentLink
import org.example.notifier.domain.position.OpenPosition
import org.example.notifier.infrastructure.external.EmailTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class EmailTemplateFactory(
    @Value("\${app.email.from}") private val defaultFrom: String,
    @Value("\${app.base-url}") private val baseUrl: String
) {

    // ========== REUSABLE EMAIL COMPONENTS ==========

    /**
     * Returns common CSS styles used across all email templates.
     * This eliminates the need to duplicate 100+ lines of CSS in each method.
     */
    private fun getCommonStyles(): String = """
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #2c3e50;
            background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
            padding: 40px 20px;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(38, 166, 154, 0.15);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #26a69a 0%, #1de9b6 100%);
            color: white;
            padding: 40px 30px;
            text-align: center;
        }
        .header h1 {
            font-size: 28px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        .header p {
            font-size: 16px;
            opacity: 0.95;
        }
        .header .emoji {
            font-size: 48px;
            margin-bottom: 16px;
        }
        .content {
            padding: 40px 30px;
        }
        .info-card {
            background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
            padding: 24px;
            border-radius: 8px;
            margin: 24px 0;
            border-left: 4px solid #26a69a;
        }
        .info-card p {
            margin: 8px 0;
            font-size: 15px;
            color: #2c3e50;
        }
        .info-row {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        .info-row:last-child {
            border-bottom: none;
        }
        .info-label {
            font-weight: 600;
            color: #455a64;
        }
        .info-value {
            color: #2c3e50;
        }
        .button-container {
            text-align: center;
            margin: 32px 0;
        }
        .button {
            display: inline-block;
            padding: 16px 40px;
            background: #26a69a;
            color: white !important;
            text-decoration: none;
            border-radius: 6px;
            font-weight: 600;
            font-size: 16px;
            box-shadow: 0 2px 8px rgba(38, 166, 154, 0.3);
        }
        .button:hover {
            background: #1e8a7e;
        }
        .footer {
            background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
            padding: 30px;
            text-align: center;
            color: #78909c;
            font-size: 14px;
            border-top: 1px solid #e0e0e0;
        }
        .footer p {
            margin: 4px 0;
        }
        .footer strong {
            color: #26a69a;
            font-weight: 600;
        }
        @media only screen and (max-width: 600px) {
            .container {
                border-radius: 0;
            }
            .content {
                padding: 24px 20px;
            }
            .header {
                padding: 32px 20px;
            }
            .button {
                padding: 14px 28px;
                font-size: 15px;
            }
        }
    """.trimIndent()

    /**
     * Builds a consistent email header with optional emoji and subtitle.
     */
    private fun buildEmailHeader(
        title: String,
        subtitle: String? = null,
        emoji: String? = null
    ): String = """
        <div class="header">
            ${emoji?.let { "<div class=\"emoji\">$it</div>" } ?: ""}
            <h1>$title</h1>
            ${subtitle?.let { "<p>$it</p>" } ?: ""}
        </div>
    """.trimIndent()

    /**
     * Builds a consistent email footer with signature.
     */
    private fun buildEmailFooter(
        signature: String = "Recruitment Team"
    ): String = """
        <div class="footer">
            <p>Best regards,</p>
            <p><strong>$signature</strong></p>
        </div>
    """.trimIndent()

    /**
     * Builds an info card with key-value pairs.
     */
    private fun buildInfoCard(items: Map<String, String>): String = """
        <div class="info-card">
            ${items.entries.joinToString("\n") { (label, value) -> 
                """<div class="info-row">
                    <span class="info-label">$label:</span>
                    <span class="info-value">$value</span>
                </div>"""
            }}
        </div>
    """.trimIndent()

    /**
     * Builds a complete email HTML template with common structure.
     * This ensures all emails have consistent structure and styling.
     */
    private fun buildFullEmailTemplate(
        headerHtml: String,
        contentHtml: String,
        footerHtml: String
    ): String = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                ${getCommonStyles()}
            </style>
        </head>
        <body>
            <div class="container">
                $headerHtml
                <div class="content">
                    $contentHtml
                </div>
                $footerHtml
            </div>
        </body>
        </html>
    """.trimIndent()

    /**
     * Helper to build a complete EmailTemplate with standard structure.
     */
    private fun buildFullEmailTemplate(
        to: String,
        subject: String,
        title: String,
        greeting: String,
        contentHtml: String,
        from: String = defaultFrom,
        signatureName: String = "Recruitment Team"
    ): EmailTemplate {
        val headerHtml = buildEmailHeader(title = title, subtitle = greeting)
        val footerHtml = buildEmailFooter(signature = signatureName)

        val fullHtml = buildFullEmailTemplate(
            headerHtml = headerHtml,
            contentHtml = contentHtml,
            footerHtml = footerHtml
        )

        return EmailTemplate(
            to = to,
            subject = subject,
            htmlContent = fullHtml,
            textContent = "Please view this email in a client that supports HTML.",
            from = from
        )
    }

    fun createCandidateReportEmail(
        to: String,
        recruiterName: String? = null,
        candidateEmail: String,
        candidateName: String? = null,
        report: AssessmentReport
    ): EmailTemplate {
        val greeting = if (recruiterName != null) "Hello $recruiterName!" else "Hello!"

        val candidateDisplayName = candidateName ?: candidateEmail
        val scoreInfo = "${report.mcDetails?.count { it.correct == true }} / ${report.mcDetails?.size}"
        val verdict = if (report.isQualified) "✅ Passed" else "❌ Failed"

        val cheatingIndicators = buildString {
            append("• 📝 Plagiarism: ${report.cheatingDetails?.plagiarism ?: "Not detected"}\n")
            append("• 📋 Pasted Code: ${report.cheatingDetails?.pastedCode ?: "Not detected"}\n")
            append("• 👀 Suspicious Activity: ${report.cheatingDetails?.suspiciousActivity ?: "false"}\n")
            append("• 🤖 AI Usage: ${report.cheatingDetails?.aiUsage ?: "Not detected"}\n")
            append("• 🧭 Tab Switching: ${report.cheatingDetails?.tabLeaving ?: "0"} times")
        }

        return EmailTemplate(
            to = to,
            subject = "Assessment Results: ${report.displayName} - ${LocalDate.now()}",
            textContent = """
            $greeting
            
            The candidate has completed the technical assessment. Here are the results:
            
            ✅ ${report.displayName} / Assessment Completed
            
            👤 Candidate: $candidateDisplayName
            📧 Email: $candidateEmail
            🎯 Score: $scoreInfo
            
            🕵️ Cheating Indicators:
            
            $cheatingIndicators
            
            📊 Summary:
            
            • 🧩 Challenge Score: ${report.codeScore}%
            • ❓ Questions Score: ${report.mcScore}%
            
            • 🧮 Final Score: ${report.finalScore}%
            • 🎯 Qualifying Score: ${report.qualifyingScore}%
            • 🧾 Verdict: $verdict
            
            The candidate has ${if (report.isQualified) "met" else "not met"} the qualifying criteria for this position.
            
            Best regards,
            Technical Assessment Team
        """.trimIndent(),
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        line-height: 1.6; 
                        color: #333; 
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header { 
                        color: #2c5aa0; 
                        border-bottom: 2px solid #2c5aa0;
                        padding-bottom: 10px;
                    }
                    .section { 
                        margin: 20px 0;
                        padding: 15px;
                        border-left: 4px solid #2c5aa0;
                        background-color: #f8f9fa;
                    }
                    .candidate-info { 
                        background-color: #e8f4fd;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 15px 0;
                    }
                    .metrics { 
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 10px;
                        margin: 15px 0;
                    }
                    .metric-item {
                        padding: 10px;
                        background-color: white;
                        border-radius: 5px;
                        border: 1px solid #ddd;
                    }
                    .verdict {
                        font-weight: bold;
                        font-size: 1.1em;
                        padding: 10px;
                        text-align: center;
                        border-radius: 5px;
                        ${if (report.isQualified) "background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb;" else "background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;"}
                    }
                    .footer { 
                        color: #666; 
                        font-size: 0.9em; 
                        margin-top: 20px;
                        padding-top: 10px;
                        border-top: 1px solid #ddd;
                    }
                    .icon { 
                        margin-right: 8px;
                    }
                </style>
            </head>
            <body>
                <h1 class="header">$greeting</h1>
                
                <p>The candidate has completed the technical assessment. Here are the detailed results:</p>
                
                <div class="section">
                    <h2>✅ ${report.displayName} - Assessment Completed</h2>
                    
                    <div class="candidate-info">
                        <p><span class="icon">👤</span> <strong>Candidate:</strong> $candidateDisplayName</p>
                        <p><span class="icon">📧</span> <strong>Email:</strong> $candidateEmail</p>
                        <p><span class="icon">🎯</span> <strong>Score:</strong> $scoreInfo</p>
                    </div>
                </div>
                
                <div class="section">
                    <h3>🕵️ Cheating Indicators</h3>
                    <p><span class="icon">📝</span> <strong>Plagiarism:</strong> ${report.cheatingDetails?.plagiarism ?: "Not detected"}</p>
                    <p><span class="icon">📋</span> <strong>Pasted Code:</strong> ${report.cheatingDetails?.pastedCode ?: "Not detected"}</p>
                    <p><span class="icon">👀</span> <strong>Suspicious Activity:</strong> ${report.cheatingDetails?.suspiciousActivity ?: "false"}</p>
                    <p><span class="icon">🤖</span> <strong>AI Usage:</strong> ${report.cheatingDetails?.aiUsage ?: "Not detected"}</p>
                    <p><span class="icon">🧭</span> <strong>Tab Switching:</strong> ${report.cheatingDetails?.tabLeaving ?: "0"} times</p>
                </div>
                
                <div class="section">
                    <h3>📊 Assessment Summary</h3>
                    <div class="metrics">
                        <div class="metric-item">
                            <span class="icon">🧩</span> <strong>Challenge Score:</strong><br>
                            ${report.codeScore}%
                        </div>
                        <div class="metric-item">
                            <span class="icon">❓</span> <strong>Questions Score:</strong><br>
                            ${report.mcScore}%
                        </div>
                        <div class="metric-item">
                            <span class="icon">🧮</span> <strong>Final Score:</strong><br>
                            ${report.finalScore}%
                        </div>
                        <div class="metric-item">
                            <span class="icon">🎯</span> <strong>Qualifying Score:</strong><br>
                            ${report.qualifyingScore}%
                        </div>
                    </div>
                    
                    <div class="verdict">
                        Final Verdict: $verdict
                    </div>
                </div>
                
                <p>The candidate has <strong>${if (report.isQualified) "met" else "not met"}</strong> the qualifying criteria for this position.</p>
                
                <div class="footer">
                    <p>Best regards,<br><strong>Technical Assessment Team</strong></p>
                </div>
            </body>
            </html>
        """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createCandidateAssessmentPendingEmail(
        to: String,
        recruiterName: String? = null,
        candidateEmail: String,
        candidateName: String? = null,
        assessmentId: String,
        timeExpired: Boolean
    ): EmailTemplate {
        val greeting = if (recruiterName != null) "Hello $recruiterName!" else "Hello!"
        val candidateDisplayName = candidateName ?: candidateEmail

        val statusIcon = if (timeExpired) "⏰" else "⏳"
        val statusText = if (timeExpired) {
            "Time Expired - Report Processing"
        } else {
            "Assessment Completed - Report Processing"
        }
        val statusDescription = if (timeExpired) {
            "The candidate's assessment time has expired. The report is still being processed by Coderbyte."
        } else {
            "The candidate has completed the assessment. The report is still being processed by Coderbyte."
        }

        return EmailTemplate(
            to = to,
            subject = "Assessment Status: $statusText - ${LocalDate.now()}",
            textContent = """
            $greeting

            $statusDescription

            $statusIcon $statusText

            👤 Candidate: $candidateDisplayName
            📧 Email: $candidateEmail
            📝 Assessment ID: $assessmentId

            ℹ️ Status: The detailed assessment report is currently being processed by Coderbyte.

            ${if (timeExpired) "⚠️ Note: The assessment time limit was reached before completion." else ""}

            Best regards,
            Technical Assessment Team
        """.trimIndent(),
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        color: #2c5aa0;
                        border-bottom: 2px solid #2c5aa0;
                        padding-bottom: 10px;
                    }
                    .status-box {
                        background-color: ${if (timeExpired) "#fff3cd" else "#d1ecf1"};
                        border: 1px solid ${if (timeExpired) "#ffc107" else "#bee5eb"};
                        border-left: 4px solid ${if (timeExpired) "#ff9800" else "#17a2b8"};
                        padding: 20px;
                        border-radius: 5px;
                        margin: 20px 0;
                    }
                    .status-box h2 {
                        margin-top: 0;
                        color: ${if (timeExpired) "#856404" else "#0c5460"};
                    }
                    .candidate-info {
                        background-color: #f8f9fa;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 15px 0;
                    }
                    .info-note {
                        background-color: #e7f3ff;
                        border-left: 4px solid #2196F3;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                    }
                    .warning-note {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 15px;
                        margin: 20px 0;
                        border-radius: 5px;
                        color: #856404;
                    }
                    .footer {
                        color: #666;
                        font-size: 0.9em;
                        margin-top: 20px;
                        padding-top: 10px;
                        border-top: 1px solid #ddd;
                    }
                    .icon {
                        margin-right: 8px;
                    }
                </style>
            </head>
            <body>
                <h1 class="header">$greeting</h1>

                <p>$statusDescription</p>

                <div class="status-box">
                    <h2>$statusIcon $statusText</h2>

                    <div class="candidate-info">
                        <p><span class="icon">👤</span> <strong>Candidate:</strong> $candidateDisplayName</p>
                        <p><span class="icon">📧</span> <strong>Email:</strong> $candidateEmail</p>
                        <p><span class="icon">📝</span> <strong>Assessment ID:</strong> $assessmentId</p>
                    </div>
                </div>

                <div class="info-note">
                    <p><strong>ℹ️ Status:</strong> The detailed assessment report is currently being processed by Coderbyte.</p>
                </div>

                ${if (timeExpired) """
                <div class="warning-note">
                    <p><strong>⚠️ Note:</strong> The assessment time limit was reached before completion.</p>
                </div>
                """ else ""}

                <div class="footer">
                    <p>Best regards,<br><strong>Technical Assessment Team</strong></p>
                </div>
            </body>
            </html>
        """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createGlobalRecipientPositionAssignmentEmail(
        to: String,
        recruiterName: String,
        positions: List<OpenPosition>,
        inviteLink: String
    ): EmailTemplate {

        val positionsText = if (positions.isNotEmpty()) {
            "\nThe following positions have been assigned:\n" +
                    positions.joinToString("\n") { "• ${it.title}" } + "\n"
        } else {
            "\nNew positions have been assigned.\n"
        }

        val positionsHtml = if (positions.isNotEmpty()) {
            """
        <div style="margin: 24px 0; background: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #26a69a;">
            <h3 style="color: #2c3e50; font-size: 18px; margin-bottom: 16px; font-weight: 600;">📋 Assigned Positions</h3>
            <ul style="list-style-type: none; padding: 0; margin: 16px 0;">
                ${positions.joinToString("") { position ->
                "<li style='padding: 8px 0; border-bottom: 1px solid #e0e0e0;'>" +
                        "<span style='color: #26a69a; margin-right: 8px;'>•</span> ${position.title}" +
                        "</li>"
            }}
            </ul>
        </div>
        """
        } else {
            ""
        }

        return EmailTemplate(
            to = to,
            subject = "Position Assignment: $recruiterName - ${LocalDate.now()}",
            textContent = """
            Position Assignment Notification
            
            Recruiter: $recruiterName
            ${positions.size} position${if (positions.size != 1) "s" else ""} assigned
            
            $positionsText
            
            The recruiter can now access these positions through the platform and start inviting candidates.
            
            Platform Access: $inviteLink
            
            This is an automated notification for your information.
            
            ---
            Score-pion Team
        """.trimIndent(),
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .header {
                        color: #26a69a;
                        border-bottom: 2px solid #26a69a;
                        padding-bottom: 10px;
                    }
                    .notification-badge {
                        display: inline-block;
                        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                        color: white;
                        padding: 6px 16px;
                        border-radius: 20px;
                        font-size: 12px;
                        font-weight: 600;
                        letter-spacing: 0.5px;
                        margin-bottom: 20px;
                    }
                    .recruiter-info {
                        background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
                        padding: 20px;
                        border-radius: 10px;
                        margin: 20px 0;
                        border-left: 4px solid #26a69a;
                    }
                    .recruiter-info h3 {
                        color: #1e40af;
                        font-size: 18px;
                        margin-bottom: 10px;
                        font-weight: 600;
                    }
                    .recruiter-info p {
                        color: #4b5563;
                        font-size: 16px;
                        margin: 0;
                        font-weight: 500;
                    }
                    .positions-section {
                        margin: 24px 0;
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #ff9800;
                    }
                    .positions-section h3 {
                        color: #2c3e50;
                        font-size: 18px;
                        margin-bottom: 16px;
                        font-weight: 600;
                    }
                    .positions-count {
                        display: inline-block;
                        background: #26a69a;
                        color: white;
                        font-size: 12px;
                        padding: 2px 8px;
                        border-radius: 10px;
                        margin-left: 10px;
                        vertical-align: middle;
                    }
                    .info-box {
                        background: linear-gradient(135deg, #fef3c7 0%, #fef9e7 100%);
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #f59e0b;
                        margin: 25px 0;
                        text-align: center;
                    }
                    .info-icon {
                        font-size: 20px;
                        margin-right: 8px;
                    }
                    .info-text {
                        font-weight: 500;
                        display: inline;
                        color: #92400e;
                    }
                    .access-box {
                        background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #10b981;
                        margin: 25px 0;
                        text-align: center;
                    }
                    .access-box a {
                        color: #065f46;
                        font-weight: 600;
                        text-decoration: none;
                        word-break: break-all;
                    }
                    .status-note {
                        text-align: center;
                        background: #f8fafc;
                        padding: 15px;
                        border-radius: 8px;
                        margin: 25px 0;
                        border: 1px dashed #cbd5e1;
                        color: #64748b;
                        font-size: 14px;
                    }
                    .footer {
                        color: #666;
                        font-size: 0.9em;
                        margin-top: 20px;
                        padding-top: 10px;
                        border-top: 1px solid #ddd;
                    }
                    @media only screen and (max-width: 600px) {
                        body {
                            padding: 15px;
                        }
                        .recruiter-info, .positions-section, .info-box, .access-box {
                            padding: 15px;
                        }
                    }
                </style>
            </head>
            <body>
                <h1 class="header">Position Assignment Notification</h1>
                
                <div class="notification-badge">FOR YOUR INFORMATION</div>
                
                <div class="recruiter-info">
                    <h3>Recruiter Assigned</h3>
                    <p>$recruiterName</p>
                </div>

                $positionsHtml

                <div class="info-box">
                    <span class="info-icon">ℹ️</span>
                    <span class="info-text">$recruiterName can now access these positions and start inviting candidates</span>
                </div>

                <div class="access-box">
                    <strong>Platform Access:</strong><br>
                    <a href="$inviteLink">$inviteLink</a>
                </div>

                <div class="status-note">
                    This is a notification email. No action is required from your side.
                </div>

                <div class="footer">
                    <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    <p style="margin-top: 10px; font-size: 12px; color: #94a3b8;">
                        This is an automated notification. Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createRecruiterPositionAssignEmail(
        to: String,
        inviteLink: String,
        positions: List<OpenPosition>
    ): EmailTemplate {

        val positionsText = if (positions.isNotEmpty()) {
            "\nYou have been assigned to the following positions:\n" +
                    positions.joinToString("\n") { "• ${it.title}" } + "\n"
        } else {
            "\nYou have been assigned to new positions.\n"
        }

        val positionsHtml = if (positions.isNotEmpty()) {
            """
        <div class="positions-section">
            <h3>📋 Assigned Positions</h3>
            <ul style="list-style-type: none; padding: 0; margin: 16px 0;">
                ${positions.joinToString("") { position ->
                "<li style='padding: 8px 0; border-bottom: 1px solid #e0e0e0;'>" +
                        "<span style='color: #26a69a; margin-right: 8px;'>•</span> ${position.title}" +
                        "</li>"
            }}
            </ul>
        </div>
        """
        } else {
            ""
        }

        return EmailTemplate(
            to = to,
            subject = "New Position Assignment - ${LocalDate.now()}",
            textContent = """
            Hello!

            The admin has assigned you to new recruiting positions.

            $positionsText

            To access your assigned positions, click the link below and sign in:
            $inviteLink

            You can now start inviting candidates to your assigned positions.

            If you have any questions, please contact your administrator.

            Best regards,
            BTS Team
        """.trimIndent(),
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #2c3e50;
                        background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
                        padding: 40px 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        box-shadow: 0 4px 20px rgba(38, 166, 154, 0.15);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #26a69a 0%, #1de9b6 100%);
                        color: white;
                        padding: 50px 30px 40px;
                        text-align: center;
                        position: relative;
                    }
                    .logo {
                        width: 120px;
                        height: 120px;
                        margin: 0 auto 20px;
                        display: block;
                    }
                    .header h1 {
                        font-size: 32px;
                        font-weight: 600;
                        margin-bottom: 8px;
                        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    }
                    .header p {
                        font-size: 16px;
                        font-weight: 400;
                        opacity: 0.95;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .invitation-box {
                        background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
                        padding: 24px;
                        border-radius: 8px;
                        margin: 24px 0;
                        border-left: 4px solid #26a69a;
                    }
                    .invitation-box p {
                        margin: 0;
                        font-size: 16px;
                        color: #2c3e50;
                        line-height: 1.6;
                    }
                    .positions-section {
                        margin: 24px 0;
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 8px;
                        border-left: 4px solid #ff9800;
                    }
                    .positions-section h3 {
                        color: #2c3e50;
                        font-size: 18px;
                        margin-bottom: 16px;
                        font-weight: 600;
                    }
                    .features {
                        margin: 32px 0;
                        background: #f8f9fa;
                        padding: 24px;
                        border-radius: 8px;
                    }
                    .features h3 {
                        color: #2c3e50;
                        font-size: 18px;
                        margin-bottom: 20px;
                        font-weight: 600;
                    }
                    .feature-item {
                        padding: 12px 0 12px 32px;
                        position: relative;
                        color: #455a64;
                        font-size: 15px;
                    }
                    .feature-item:before {
                        content: '✓';
                        position: absolute;
                        left: 0;
                        color: #26a69a;
                        font-weight: bold;
                        font-size: 20px;
                        width: 24px;
                        height: 24px;
                        background: rgba(38, 166, 154, 0.1);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .button-container {
                        text-align: center;
                        margin: 32px 0;
                    }
                    .button {
                        display: inline-block;
                        padding: 16px 40px;
                        background: #26a69a;
                        color: white !important;
                        text-decoration: none;
                        border-radius: 6px;
                        font-weight: 600;
                        font-size: 16px;
                        box-shadow: 0 2px 8px rgba(38, 166, 154, 0.3);
                        border: none;
                    }
                    .info-box {
                        background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
                        padding: 16px 20px;
                        border-radius: 8px;
                        border-left: 4px solid #2196f3;
                        margin: 24px 0;
                        text-align: center;
                    }
                    .info-icon {
                        font-size: 20px;
                        margin-right: 8px;
                    }
                    .info-text {
                        font-weight: 500;
                        display: inline;
                        color: #1976d2;
                    }
                    .footer {
                        background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
                        padding: 30px;
                        text-align: center;
                        color: #78909c;
                        font-size: 14px;
                        border-top: 1px solid #e0e0e0;
                    }
                    .footer p {
                        margin: 4px 0;
                    }
                    .footer strong {
                        color: #26a69a;
                        font-weight: 600;
                    }
                    @media only screen and (max-width: 600px) {
                        .container {
                            border-radius: 0;
                        }
                        .content {
                            padding: 24px 20px;
                        }
                        .header {
                            padding: 40px 20px 30px;
                        }
                        .logo {
                            width: 100px;
                            height: 100px;
                        }
                        .header h1 {
                            font-size: 26px;
                        }
                        .button {
                            padding: 14px 28px;
                            font-size: 15px;
                        }
                        .features, .positions-section {
                            padding: 20px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="${baseUrl}/assets/images/logos/scorpion-logo-v2.png"
                             alt="Score-pion Logo"
                             class="logo">
                        <h1>New Position Assignment</h1>
                        <p>You have been assigned to new recruiting positions</p>
                    </div>

                    <div class="content">
                        <div class="invitation-box">
                            <p><strong>The admin</strong> has assigned you to new recruiting positions on our platform.</p>
                        </div>

                        $positionsHtml

                        <p style="text-align: center; margin: 24px 0; color: #455a64;">To access your assigned positions and start inviting candidates, click the button below:</p>

                        <div class="button-container">
                            <a href="$inviteLink" class="button">Access Platform</a>
                        </div>

                        <div class="info-box">
                            <span class="info-icon">🚀</span>
                            <span class="info-text">You can now start inviting candidates to your assigned positions</span>
                        </div>

                        <p style="text-align: center; color: #78909c; margin-top: 32px; font-size: 14px;">If you have any questions, please contact your administrator.</p>
                    </div>

                    <div class="footer">
                        <p>Best regards,</p>
                        <p><strong>Score-pion Team</strong></p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent(),
            from = defaultFrom
        )
    }
    fun createGlobalRecipientPositionCreationEmail(
        to: String,
        positionTitle: String,
        positionDescription: String?,
        createdBy: String,
        positionLink: String,
        isExternal: Boolean = false,
        assessmentNames: List<String> = emptyList()
    ): EmailTemplate {

        val positionType = if (isExternal) "External Position" else "Internal Position"

        val assessmentsText = if (assessmentNames.isNotEmpty()) {
            "\n📋 Associated Assessments:\n" +
                    assessmentNames.joinToString("\n") { "• $it" } + "\n"
        } else {
            "\n📋 No assessments have been assigned to this position yet.\n"
        }

        val assessmentsHtml = if (assessmentNames.isNotEmpty()) {
            """
        <div class="assessments-section">
            <h3>📋 Associated Assessments</h3>
            <ul style="list-style-type: none; padding: 0; margin: 16px 0;">
                ${assessmentNames.joinToString("") { name ->
                "<li style='padding: 12px 0; border-bottom: 1px solid #e0e0e0; display: flex; justify-content: space-between; align-items: center;'>" +
                        "<span>" +
                        "<span style='color: #26a69a; margin-right: 8px;'>•</span> " +
                        "<strong style='color: #2c3e50;'>$name</strong>" +
                        "</span>" +
                        "</li>"
            }}
            </ul>
        </div>
        """
        } else {
            """
        <div class="assessments-section" style="background: linear-gradient(135deg, #fff3e0 0%, #fff8e1 100%); padding: 20px; border-radius: 8px; border-left: 4px solid #ffb74d; margin: 24px 0;">
            <h3 style="color: #e65100; font-size: 18px; margin-bottom: 12px; display: flex; align-items: center;">
                <span style="margin-right: 8px;">⚠️</span> No Assessments Assigned
            </h3>
            <p style="color: #5d4037; margin: 0; font-size: 14px;">This position doesn't have any assessments assigned yet. You can add assessments from the position management page.</p>
        </div>
        """
        }

        val descriptionHtml = if (!positionDescription.isNullOrBlank()) {
            """
        <div class="description-section" style="margin: 20px 0;">
            <h4 style="color: #2c3e50; font-size: 16px; margin-bottom: 8px; font-weight: 600;">Description</h4>
            <div style="background: #f8f9fa; padding: 16px; border-radius: 6px; border-left: 3px solid #78909c;">
                <p style="color: #455a64; margin: 0; line-height: 1.6;">${positionDescription.replace("\n", "<br>")}</p>
            </div>
        </div>
        """
        } else {
            """
        <div class="description-section" style="margin: 20px 0;">
            <h4 style="color: #2c3e50; font-size: 16px; margin-bottom: 8px; font-weight: 600;">Description</h4>
            <p style="color: #78909c; font-style: italic; margin: 0;">No description provided</p>
        </div>
        """
        }

        val positionTypeBadge = if (isExternal) {
            "<span style='background-color: #2196f3; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; margin-left: 8px;'>EXTERNAL</span>"
        } else {
            "<span style='background-color: #4caf50; color: white; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; margin-left: 8px;'>INTERNAL</span>"
        }

        return EmailTemplate(
            to = to,
            subject = "New Position Created: $positionTitle - ${LocalDate.now()}",
            textContent = """
            NEW POSITION CREATED

            A new position has been created on the recruitment platform.

            📝 Position Details:
            • Title: $positionTitle
            • Type: $positionType
            • Created By: $createdBy
            • Created On: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}

            ${if (!positionDescription.isNullOrBlank()) "• Description: $positionDescription\n" else ""}
            $assessmentsText

            🔗 Quick Actions:
            • View Position Details: $positionLink
            • Manage Position: ${positionLink.replace("/view", "/edit")}

            This position is now available in the system and will be visible to assigned recruiters.

            Best regards,
            Score-pion Team
        """.trimIndent(),
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #2c3e50;
                        background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
                        padding: 40px 20px;
                    }
                    .container {
                        max-width: 650px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .logo {
                        width: 100px;
                        height: 100px;
                        margin: 0 auto 20px;
                        display: block;
                    }
                    .header h1 {
                        font-size: 28px;
                        font-weight: 600;
                        margin-bottom: 12px;
                    }
                    .header p {
                        font-size: 16px;
                        opacity: 0.9;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .info-card {
                        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
                        padding: 24px;
                        border-radius: 8px;
                        border-left: 4px solid #667eea;
                        margin: 20px 0;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
                    }
                    .info-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 10px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .info-label {
                        font-weight: 600;
                        color: #5a6c7d;
                        min-width: 140px;
                    }
                    .info-value {
                        color: #2c3e50;
                        flex-grow: 1;
                    }
                    .actions {
                        display: flex;
                        gap: 12px;
                        margin: 30px 0;
                        flex-wrap: wrap;
                    }
                    .btn {
                        display: inline-block;
                        padding: 14px 28px;
                        background: #667eea;
                        color: white !important;
                        text-decoration: none;
                        border-radius: 6px;
                        font-weight: 600;
                        font-size: 15px;
                        text-align: center;
                        flex: 1;
                        min-width: 180px;
                        transition: all 0.3s ease;
                    }
                    .btn:hover {
                        background: #5a67d8;
                        transform: translateY(-2px);
                        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
                    }
                    .btn-secondary {
                        background: #48bb78;
                    }
                    .btn-secondary:hover {
                        background: #38a169;
                        box-shadow: 0 4px 12px rgba(72, 187, 120, 0.3);
                    }
                    .footer {
                        background: #f8f9fa;
                        padding: 30px;
                        text-align: center;
                        color: #718096;
                        font-size: 14px;
                        border-top: 1px solid #e2e8f0;
                    }
                    .timestamp {
                        text-align: center;
                        color: #a0aec0;
                        font-size: 13px;
                        margin: 20px 0;
                        padding: 12px;
                        background: #f7fafc;
                        border-radius: 6px;
                    }
                    @media only screen and (max-width: 600px) {
                        .container {
                            border-radius: 0;
                        }
                        .content {
                            padding: 24px 20px;
                        }
                        .header {
                            padding: 30px 20px;
                        }
                        .header h1 {
                            font-size: 24px;
                        }
                        .btn {
                            min-width: 100%;
                            margin-bottom: 10px;
                        }
                        .info-row {
                            flex-direction: column;
                            align-items: flex-start;
                        }
                        .info-label {
                            margin-bottom: 4px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="${baseUrl}/assets/images/logos/scorpion-logo-v2.png"
                             alt="Score-pion Logo"
                             class="logo">
                        <h1>New Position Created</h1>
                        <p>A new position has been added to the recruitment platform</p>
                    </div>

                    <div class="content">
                        <div class="info-card">
                            <div class="info-row">
                                <span class="info-label">Position Title:</span>
                                <span class="info-value">
                                    <strong>${positionTitle}</strong>
                                    ${positionTypeBadge}
                                </span>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label">Created By:</span>
                                <span class="info-value">
                                    <strong>${createdBy}</strong>
                                </span>
                            </div>
                            
                            <div class="info-row">
                                <span class="info-label">Created On:</span>
                                <span class="info-value">
                                    ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}
                                </span>
                            </div>
                        </div>

                        ${descriptionHtml}

                        ${assessmentsHtml}

                        <div class="timestamp">
                            📅 This notification was generated automatically on 
                            ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm:ss"))}
                        </div>

                        <div class="actions">
                            <a href="${positionLink}" class="btn">View Position Details</a>
                        </div>

                        <p style="text-align: center; color: #718096; margin-top: 24px; font-size: 14px;">
                            This position is now available in the system and can be assigned to recruiters for candidate management.
                        </p>
                    </div>

                    <div class="footer">
                        <p>Best regards,</p>
                        <p><strong>Score-pion Team</strong></p>
                        <p style="margin-top: 16px; font-size: 13px; color: #a0aec0;">
                            This is an automated notification. Please do not reply to this email.
                        </p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createRecruiterInvitationEmail(
        to: String,
        adminName: String? = null,
        inviteLink: String
    ): EmailTemplate {
        val greeting = if (adminName != null) "Hello!" else "Hello!"
        val invitedBy = if (adminName != null) adminName else "the admin"

        return EmailTemplate(
            to = to,
            subject = "You've been invited to join as a Recruiter - ${LocalDate.now()}",
            textContent = """
                $greeting

                $invitedBy has invited you to join as a Recruiter on our platform.

                As a Recruiter, you will be able to:
                • Invite candidates to technical assessments
                • View assessment results
                • Manage candidate applications

                To accept this invitation, click the link below and sign in:
                $inviteLink

                This invitation will expire in 3 days.

                If you have any questions, please contact your administrator.

                Best regards,
                BTS Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        * {
                            margin: 0;
                            padding: 0;
                            box-sizing: border-box;
                        }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #2c3e50;
                            background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
                            padding: 40px 20px;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 12px;
                            box-shadow: 0 4px 20px rgba(38, 166, 154, 0.15);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #26a69a 0%, #1de9b6 100%);
                            color: white;
                            padding: 50px 30px 40px;
                            text-align: center;
                            position: relative;
                        }
                        .logo {
                            width: 120px;
                            height: 120px;
                            margin: 0 auto 20px;
                            display: block;
                        }
                        .header h1 {
                            font-size: 32px;
                            font-weight: 600;
                            margin-bottom: 8px;
                            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                        }
                        .header p {
                            font-size: 16px;
                            font-weight: 400;
                            opacity: 0.95;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .invitation-box {
                            background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
                            padding: 24px;
                            border-radius: 8px;
                            margin: 24px 0;
                            border-left: 4px solid #26a69a;
                        }
                        .invitation-box p {
                            margin: 0;
                            font-size: 16px;
                            color: #2c3e50;
                            line-height: 1.6;
                        }
                        .features {
                            margin: 32px 0;
                            background: #f8f9fa;
                            padding: 24px;
                            border-radius: 8px;
                        }
                        .features h3 {
                            color: #2c3e50;
                            font-size: 18px;
                            margin-bottom: 20px;
                            font-weight: 600;
                        }
                        .feature-item {
                            padding: 12px 0 12px 32px;
                            position: relative;
                            color: #455a64;
                            font-size: 15px;
                        }
                        .feature-item:before {
                            content: '✓';
                            position: absolute;
                            left: 0;
                            color: #26a69a;
                            font-weight: bold;
                            font-size: 20px;
                            width: 24px;
                            height: 24px;
                            background: rgba(38, 166, 154, 0.1);
                            border-radius: 50%;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .button-container {
                            text-align: center;
                            margin: 32px 0;
                        }
                        .button {
                            display: inline-block;
                            padding: 16px 40px;
                            background: #26a69a;
                            color: white !important;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 16px;
                            box-shadow: 0 2px 8px rgba(38, 166, 154, 0.3);
                            border: none;
                        }
                        .warning {
                            color: #f57c00;
                            background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
                            padding: 16px 20px;
                            border-radius: 8px;
                            border-left: 4px solid #ff9800;
                            margin: 24px 0;
                            text-align: center;
                        }
                        .warning-icon {
                            font-size: 20px;
                            margin-right: 8px;
                        }
                        .warning-text {
                            font-weight: 500;
                            display: inline;
                        }
                        .footer {
                            background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
                            padding: 30px;
                            text-align: center;
                            color: #78909c;
                            font-size: 14px;
                            border-top: 1px solid #e0e0e0;
                        }
                        .footer p {
                            margin: 4px 0;
                        }
                        .footer strong {
                            color: #26a69a;
                            font-weight: 600;
                        }
                        @media only screen and (max-width: 600px) {
                            .container {
                                border-radius: 0;
                            }
                            .content {
                                padding: 24px 20px;
                            }
                            .header {
                                padding: 40px 20px 30px;
                            }
                            .logo {
                                width: 100px;
                                height: 100px;
                            }
                            .header h1 {
                                font-size: 26px;
                            }
                            .button {
                                padding: 14px 28px;
                                font-size: 15px;
                            }
                            .features {
                                padding: 20px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <img src="${baseUrl}/assets/images/logos/scorpion-logo-v2.png"
                                 alt="Score-pion Logo"
                                 class="logo">
                            <h1>$greeting</h1>
                            <p>Welcome to the Score-pion platform!</p>
                        </div>

                        <div class="content">
                            <div class="invitation-box">
                                <p><strong>$invitedBy</strong> has invited you to join as a Recruiter on our platform.</p>
                            </div>

                            <div class="features">
                                <h3>As a Recruiter, you will be able to:</h3>
                                <div class="feature-item">Invite candidates to positions</div>
                                <div class="feature-item">View assessment results</div>
                                <div class="feature-item">Manage candidate applications</div>
                            </div>

                            <p style="text-align: center; margin: 24px 0; color: #455a64;">To accept this invitation, click the button below and sign in:</p>

                            <div class="button-container">
                                <a href="$inviteLink" class="button">Sign in</a>
                            </div>

                            <div class="warning">
                                <span class="warning-icon">⏰</span>
                                <span class="warning-text">This invitation will expire in 3 days.</span>
                            </div>

                            <p style="text-align: center; color: #78909c; margin-top: 32px; font-size: 14px;">If you have any questions, please contact your administrator.</p>
                        </div>

                        <div class="footer">
                            <p>Best regards,</p>
                            <p><strong>Score-pion Team</strong></p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createAdminInvitationEmail(
        to: String,
        invitedBy: String? = null,
        inviteLink: String
    ): EmailTemplate {
        val greeting = if (invitedBy != null) "Hello!" else "Hello!"
        val inviter = if (invitedBy != null) invitedBy else "an administrator"

        return EmailTemplate(
            to = to,
            subject = "Invitation to join as an Administrator - Score-pion",
            textContent = """
                $greeting

                $inviter has invited you to join as an Administrator on Score-pion.

                As an Administrator, you will be able to:
                • Manage administrative access for other users
                • Create and manage recruitment positions
                • Oversee all recruiter activities and assessments
                • Configure global system settings

                To accept this invitation, click the link below and sign in:
                $inviteLink

                This invitation will expire in 3 days.

                Best regards,
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        ${getCommonStyles()}
                        .invitation-box {
                            background: linear-gradient(135deg, #e0f2f1 0%, #ffffff 100%);
                            padding: 24px;
                            border-radius: 8px;
                            margin: 24px 0;
                            border-left: 4px solid #00796b;
                        }
                        .invitation-box p {
                            margin: 0;
                            font-size: 16px;
                            color: #2c3e50;
                            line-height: 1.6;
                        }
                        .features {
                            margin: 32px 0;
                            background: #f1f8f7;
                            padding: 24px;
                            border-radius: 8px;
                        }
                        .features h3 {
                            color: #004d40;
                            font-size: 18px;
                            margin-bottom: 20px;
                            font-weight: 600;
                        }
                        .feature-item {
                            padding: 12px 0 12px 32px;
                            position: relative;
                            color: #37474f;
                            font-size: 15px;
                        }
                        .feature-item:before {
                            content: '🛡️';
                            position: absolute;
                            left: 0;
                            top: 50%;
                            transform: translateY(-50%);
                            font-size: 18px;
                        }
                        .button-container {
                            text-align: center;
                            margin: 32px 0;
                        }
                        .button {
                            display: inline-block;
                            padding: 16px 40px;
                            background: #00796b;
                            color: white !important;
                            text-decoration: none;
                            border-radius: 6px;
                            font-weight: 600;
                            font-size: 16px;
                            box-shadow: 0 4px 12px rgba(0, 121, 107, 0.2);
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header" style="background: linear-gradient(135deg, #00796b 0%, #004d40 100%);">
                            <img src="${baseUrl}/assets/images/logos/scorpion-logo-v2.png"
                                 alt="Score-pion Logo"
                                 class="logo">
                            <h1>Welcome to Score-pion</h1>
                            <p>Administrative Invitation</p>
                        </div>

                        <div class="content">
                            <div class="invitation-box">
                                <p><strong>$inviter</strong> has invited you to join as an <strong>Administrator</strong> on our platform.</p>
                            </div>

                            <div class="features">
                                <h3>Administrative Privileges:</h3>
                                <div class="feature-item">Manage admin and recruiter access</div>
                                <div class="feature-item">Create and oversee recruitment positions</div>
                                <div class="feature-item">Access comprehensive assessment reports</div>
                                <div class="feature-item">Configure global system parameters</div>
                            </div>

                            <div class="button-container">
                                <a href="$inviteLink" class="button">Accept Admin Invitation</a>
                            </div>

                            <div class="warning">
                                <span class="warning-icon">⏰</span>
                                <span class="warning-text">This invitation will expire in 3 days.</span>
                            </div>
                        </div>

                        <div class="footer">
                            <p>Best regards,</p>
                            <p><strong>Score-pion Team</strong></p>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createApplicantRejectedEmail(
        to: String,
        applicantName: String? = null,
        positionTitle: String,
        recruiterName: String? = null
    ): EmailTemplate {
        val greeting = if (applicantName != null) "Hello $applicantName," else "Hello,"

        val contentHtml = """
            <div class="content-box">
                <p>Thank you for your interest in the <strong>$positionTitle</strong> position and for taking the time to participate in our selection process.</p>

                <p>We appreciate the opportunity to review your application and qualifications. After careful consideration, we regret to inform you that we have decided not to move forward with your application at this time.</p>

                <p>This was a difficult decision, as we received applications from many qualified candidates. We will keep your resume on file for future openings that may be a better fit for your skills and experience.</p>

                <p>We wish you the best of luck in your job search and future professional endeavors.</p>
            </div>
        """.trimIndent()

        return buildFullEmailTemplate(
            to = to,
            subject = "Update on your application for $positionTitle",
            title = "Application Update",
            greeting = greeting,
            contentHtml = contentHtml,
            signatureName = recruiterName ?: "Recruitment Team"
        )
    }

    fun createApplicantApprovedEmail(
        to: String,
        applicantName: String? = null,
        positionTitle: String,
        recruiterName: String? = null,
    ): EmailTemplate {
        val greeting = if (applicantName != null) "Hello $applicantName!" else "Hello!"

        val assessmentsListHtml =
            """
            <div class="assessments-section">
                <p>The assessment links for the <strong>$positionTitle</strong> position will be sent to you shortly via email from Coderbyte.</p>
            </div>
            """.trimIndent()


        val assessmentsListText = "The assessment links for the $positionTitle position will be sent to you shortly via email from Coderbyte."


        return EmailTemplate(
            to = to,
            subject = "🎉 Congratulations! Your Application for $positionTitle has been Approved",
            textContent = """
            $greeting
            
            Great news! Your application for the $positionTitle position has been reviewed and approved by ${recruiterName ?: "our recruitment team"}.
            
            We were impressed with your CV and would like to move forward with the next step in our hiring process.
            
            $assessmentsListText
            
            Next Steps:
            1. Check your email inbox for assessment invitations from Coderbyte
            2. Complete the technical assessments at your convenience
            3. Our team will review your results and get back to you
            
            If you have any questions or concerns, please don't hesitate to reach out.
            
            We look forward to seeing your performance in the assessments!
            
            Best regards,
            ${recruiterName ?: "Recruitment Team"}
        """.trimIndent(),
            htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #2c3e50;
                        background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
                        padding: 40px 20px;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 12px;
                        box-shadow: 0 4px 20px rgba(38, 166, 154, 0.15);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, #26a69a 0%, #1de9b6 100%);
                        color: white;
                        padding: 40px 30px;
                        text-align: center;
                    }
                    .header h1 {
                        font-size: 28px;
                        font-weight: 600;
                        margin-bottom: 8px;
                    }
                    .header .emoji {
                        font-size: 48px;
                        margin-bottom: 16px;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .congratulations-box {
                        background: linear-gradient(135deg, #d4f1d4 0%, #e8f8e8 100%);
                        padding: 24px;
                        border-radius: 8px;
                        margin: 24px 0;
                        border-left: 4px solid #4caf50;
                    }
                    .congratulations-box p {
                        margin: 0;
                        font-size: 16px;
                        color: #2c3e50;
                        line-height: 1.6;
                    }
                    .assessments-section {
                        margin: 32px 0;
                        background: #f8f9fa;
                        padding: 24px;
                        border-radius: 8px;
                    }
                    .assessments-section h3 {
                        color: #2c3e50;
                        font-size: 18px;
                        margin-bottom: 16px;
                        font-weight: 600;
                    }
                    .assessments-list {
                        margin: 20px 0;
                    }
                    .assessment-item {
                        background: white;
                        padding: 16px;
                        border-radius: 6px;
                        margin-bottom: 12px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        border: 1px solid #e0e0e0;
                    }
                    .assessment-name {
                        font-weight: 500;
                        color: #2c3e50;
                    }
                    .assessment-link {
                        background: #26a69a;
                        color: white !important;
                        text-decoration: none;
                        padding: 8px 16px;
                        border-radius: 4px;
                        font-size: 14px;
                        font-weight: 500;
                        transition: background 0.3s;
                    }
                    .assessment-link:hover {
                        background: #1e8a7e;
                    }
                    .next-steps {
                        margin: 32px 0;
                        background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
                        padding: 24px;
                        border-radius: 8px;
                        border-left: 4px solid #ff9800;
                    }
                    .next-steps h3 {
                        color: #2c3e50;
                        font-size: 18px;
                        margin-bottom: 16px;
                        font-weight: 600;
                    }
                    .step-item {
                        padding: 8px 0 8px 32px;
                        position: relative;
                        color: #455a64;
                        font-size: 15px;
                    }
                    .step-item:before {
                        content: attr(data-step);
                        position: absolute;
                        left: 0;
                        color: #ff9800;
                        font-weight: bold;
                        width: 24px;
                        height: 24px;
                        background: rgba(255, 152, 0, 0.1);
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 14px;
                    }
                    .footer {
                        background: linear-gradient(135deg, #f5f5f5 0%, #fafafa 100%);
                        padding: 30px;
                        text-align: center;
                        color: #78909c;
                        font-size: 14px;
                        border-top: 1px solid #e0e0e0;
                    }
                    .footer p {
                        margin: 4px 0;
                    }
                    .footer strong {
                        color: #26a69a;
                        font-weight: 600;
                    }
                    @media only screen and (max-width: 600px) {
                        .container {
                            border-radius: 0;
                        }
                        .content {
                            padding: 24px 20px;
                        }
                        .header {
                            padding: 32px 20px;
                        }
                        .assessment-item {
                            flex-direction: column;
                            gap: 12px;
                            align-items: flex-start;
                        }
                        .assessment-link {
                            width: 100%;
                            text-align: center;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="emoji">🎉</div>
                        <h1>$greeting</h1>
                        <p>Application Approved!</p>
                    </div>

                    <div class="content">
                        <div class="congratulations-box">
                            <p><strong>Great news!</strong> Your application for the <strong>$positionTitle</strong> position has been reviewed and approved by ${recruiterName ?: "our recruitment team"}.</p>
                        </div>

                        <p style="margin: 24px 0; color: #455a64;">
                            We were impressed with your CV and would like to move forward with the next step in our hiring process.
                        </p>

                        $assessmentsListHtml

                        <div class="next-steps">
                            <h3>📋 Next Steps</h3>
                            <div class="step-item" data-step="1">Check your email inbox for assessment invitations from Coderbyte</div>
                            <div class="step-item" data-step="2">Complete the technical assessments at your convenience</div>
                            <div class="step-item" data-step="3">Our team will review your results and get back to you</div>
                        </div>

                        <p style="text-align: center; color: #78909c; margin-top: 32px; font-size: 14px;">
                            If you have any questions or concerns, please don't hesitate to reach out.
                        </p>

                        <p style="text-align: center; color: #26a69a; margin-top: 16px; font-weight: 600; font-size: 16px;">
                            We look forward to seeing your performance in the assessments!
                        </p>
                    </div>

                    <div class="footer">
                        <p>Best regards,</p>
                        <p><strong>${recruiterName ?: "Recruitment Team"}</strong></p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent(),
            from = defaultFrom
        )
    }

    /**
     * Email template for global recipients when a candidate is invited
     */
    fun createGlobalRecipientInvitationEmail(
        to: String,
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String,
        assessmentsCount: Int
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Candidate Invited: $candidateName - $positionTitle",
            textContent = """
                New Candidate Invited

                Candidate: $candidateName
                Email: $candidateEmail
                Position: $positionTitle
                Assessments: $assessmentsCount
                Invited by: $recruiterName

                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            color: #26a69a;
                            border-bottom: 2px solid #26a69a;
                            padding-bottom: 10px;
                        }
                        .section {
                            margin: 20px 0;
                            padding: 15px;
                            border-left: 4px solid #26a69a;
                            background-color: #f8f9fa;
                        }
                        .info-item {
                            padding: 8px 0;
                        }
                        .footer {
                            color: #666;
                            font-size: 0.9em;
                            margin-top: 20px;
                            padding-top: 10px;
                            border-top: 1px solid #ddd;
                        }
                        .icon {
                            margin-right: 8px;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="header">New Candidate Invited</h1>

                    <p>A new candidate has been invited to complete technical assessments.</p>

                    <div class="section">
                        <div class="info-item">
                            <span class="icon">👤</span> <strong>Candidate:</strong> $candidateName
                        </div>
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Email:</strong> $candidateEmail
                        </div>
                        <div class="info-item">
                            <span class="icon">💼</span> <strong>Position:</strong> $positionTitle
                        </div>
                        <div class="info-item">
                            <span class="icon">📝</span> <strong>Assessments:</strong> $assessmentsCount
                        </div>
                        <div class="info-item">
                            <span class="icon">👨‍💼</span> <strong>Invited by:</strong> $recruiterName
                        </div>
                    </div>

                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    /**
     * Email template for global recipients when assessment is completed WITH report
     */
    fun createGlobalRecipientAssessmentCompletedEmail(
        to: String,
        candidateEmail: String,
        candidateName: String?,
        report: AssessmentReport,
        positionTitle: String?,
        timeExpired: Boolean,
        recruiterName: String? = null
    ): EmailTemplate {
        val displayName = candidateName ?: candidateEmail
        val positionInfo = positionTitle?.let { " - $it" } ?: ""
        val statusIcon = if (timeExpired) "⏰" else "✅"
        val statusText = if (timeExpired) "Time Expired" else "Completed"
        val verdict = if (report.isQualified) "✅ Passed" else "❌ Failed"
        val scoreInfo = "${report.mcDetails?.count { it.correct == true }} / ${report.mcDetails?.size}"

        return EmailTemplate(
            to = to,
            subject = "Assessment $statusText: $displayName$positionInfo",
            textContent = """
                Assessment $statusText

                Candidate: $displayName
                Email: $candidateEmail
                ${positionTitle?.let { "Position: $it\n" } ?: ""}${recruiterName?.let { "Recruiter: $it\n" } ?: ""}Assessment: ${report.displayName}

                Results:
                • Final Score: ${report.finalScore}%
                • Qualifying Score: ${report.qualifyingScore}%
                • Verdict: ${if (report.isQualified) "Passed" else "Failed"}

                Cheating Indicators:
                • Plagiarism: ${report.cheatingDetails?.plagiarism ?: "Not detected"}
                • Pasted Code: ${report.cheatingDetails?.pastedCode ?: "Not detected"}
                • AI Usage: ${report.cheatingDetails?.aiUsage ?: "Not detected"}
                • Tab Switching: ${report.cheatingDetails?.tabLeaving ?: 0} times

                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            color: #26a69a;
                            border-bottom: 2px solid #26a69a;
                            padding-bottom: 10px;
                        }
                        .section {
                            margin: 20px 0;
                            padding: 15px;
                            border-left: 4px solid #26a69a;
                            background-color: #f8f9fa;
                        }
                        .candidate-info {
                            background-color: #e0f7f4;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 15px 0;
                        }
                        .metrics {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 10px;
                            margin: 15px 0;
                        }
                        .metric-item {
                            padding: 10px;
                            background-color: white;
                            border-radius: 5px;
                            border: 1px solid #ddd;
                        }
                        .verdict {
                            font-weight: bold;
                            font-size: 1.1em;
                            padding: 10px;
                            text-align: center;
                            border-radius: 5px;
                            ${if (report.isQualified) "background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb;" else "background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;"}
                        }
                        .footer {
                            color: #666;
                            font-size: 0.9em;
                            margin-top: 20px;
                            padding-top: 10px;
                            border-top: 1px solid #ddd;
                        }
                        .icon {
                            margin-right: 8px;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="header">Assessment Report</h1>

                    <p>The candidate has completed the technical assessment. Here are the detailed results:</p>

                    <div class="section">
                        <h2>$statusIcon ${report.displayName} - Assessment $statusText</h2>

                        <div class="candidate-info">
                            <p><span class="icon">👤</span> <strong>Candidate:</strong> $displayName</p>
                            <p><span class="icon">📧</span> <strong>Email:</strong> $candidateEmail</p>
                            ${positionTitle?.let { "<p><span class=\"icon\">💼</span> <strong>Position:</strong> $it</p>" } ?: ""}
                            ${recruiterName?.let { "<p><span class=\"icon\">🧑‍💼</span> <strong>Recruiter:</strong> $it</p>" } ?: ""}
                            <p><span class="icon">🎯</span> <strong>Score:</strong> $scoreInfo</p>
                        </div>
                    </div>

                    <div class="section">
                        <h3>🕵️ Cheating Indicators</h3>
                        <p><span class="icon">📝</span> <strong>Plagiarism:</strong> ${report.cheatingDetails?.plagiarism ?: "Not detected"}</p>
                        <p><span class="icon">📋</span> <strong>Pasted Code:</strong> ${report.cheatingDetails?.pastedCode ?: "Not detected"}</p>
                        <p><span class="icon">👀</span> <strong>Suspicious Activity:</strong> ${report.cheatingDetails?.suspiciousActivity ?: "false"}</p>
                        <p><span class="icon">🤖</span> <strong>AI Usage:</strong> ${report.cheatingDetails?.aiUsage ?: "Not detected"}</p>
                        <p><span class="icon">🧭</span> <strong>Tab Switching:</strong> ${report.cheatingDetails?.tabLeaving ?: "0"} times</p>
                    </div>

                    <div class="section">
                        <h3>📊 Assessment Summary</h3>
                        <div class="metrics">
                            <div class="metric-item">
                                <span class="icon">🧩</span> <strong>Challenge Score:</strong><br>
                                ${report.codeScore}%
                            </div>
                            <div class="metric-item">
                                <span class="icon">❓</span> <strong>Questions Score:</strong><br>
                                ${report.mcScore}%
                            </div>
                            <div class="metric-item">
                                <span class="icon">🧮</span> <strong>Final Score:</strong><br>
                                ${report.finalScore}%
                            </div>
                            <div class="metric-item">
                                <span class="icon">🎯</span> <strong>Qualifying Score:</strong><br>
                                ${report.qualifyingScore}%
                            </div>
                        </div>

                        <div class="verdict">
                            Final Verdict: $verdict
                        </div>
                    </div>

                    <p>The candidate has <strong>${if (report.isQualified) "met" else "not met"}</strong> the qualifying criteria for this position.</p>

                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    /**
     * Email template for global recipients when assessment is completed WITHOUT report (still processing)
     */
    fun createGlobalRecipientAssessmentPendingEmail(
        to: String,
        candidateEmail: String,
        candidateName: String?,
        assessmentId: String,
        positionTitle: String?,
        timeExpired: Boolean,
        recruiterName: String? = null
    ): EmailTemplate {
        val displayName = candidateName ?: candidateEmail
        val positionInfo = positionTitle?.let { " - $it" } ?: ""
        val statusIcon = if (timeExpired) "⏰" else "⏳"
        val statusText = if (timeExpired) "Time Expired - Report Processing" else "Completed - Report Processing"

        return EmailTemplate(
            to = to,
            subject = "Assessment Pending: $displayName$positionInfo",
            textContent = """
                Assessment $statusText

                Candidate: $displayName
                Email: $candidateEmail
                ${positionTitle?.let { "Position: $it\n" } ?: ""}${recruiterName?.let { "Recruiter: $it\n" } ?: ""}Assessment ID: $assessmentId

                Status: The assessment report is still being processed by Coderbyte.
                ${if (timeExpired) "Note: Assessment time limit was reached.\n" else ""}
                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            color: #ff9800;
                            border-bottom: 2px solid #ff9800;
                            padding-bottom: 10px;
                        }
                        .section {
                            margin: 20px 0;
                            padding: 15px;
                            border-left: 4px solid #ff9800;
                            background-color: #fff3e0;
                        }
                        .candidate-info {
                            background-color: #fff8e1;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 15px 0;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 15px 0;
                            border-radius: 5px;
                            color: #856404;
                        }
                        .footer {
                            color: #666;
                            font-size: 0.9em;
                            margin-top: 20px;
                            padding-top: 10px;
                            border-top: 1px solid #ddd;
                        }
                        .icon {
                            margin-right: 8px;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="header">$statusIcon Assessment $statusText</h1>

                    <p>The candidate has ${if (timeExpired) "reached the time limit" else "completed"} the assessment. The detailed report is still being processed.</p>

                    <div class="section">
                        <div class="candidate-info">
                            <p><span class="icon">👤</span> <strong>Candidate:</strong> $displayName</p>
                            <p><span class="icon">📧</span> <strong>Email:</strong> $candidateEmail</p>
                            ${positionTitle?.let { "<p><span class=\"icon\">💼</span> <strong>Position:</strong> $it</p>" } ?: ""}
                            ${recruiterName?.let { "<p><span class=\"icon\">🧑‍💼</span> <strong>Recruiter:</strong> $it</p>" } ?: ""}
                            <p><span class="icon">📝</span> <strong>Assessment ID:</strong> $assessmentId</p>
                        </div>
                    </div>

                    <div class="warning">
                        <p><strong>ℹ️ Status:</strong> The assessment report is still being processed by Coderbyte.</p>
                    </div>

                    ${if (timeExpired) """
                    <div class="warning" style="background-color: #f8d7da; border-left-color: #dc3545; color: #721c24;">
                        <p><strong>⚠️ Note:</strong> The assessment time limit was reached before completion.</p>
                    </div>
                    """ else ""}

                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }


    /**
     * Main unified method to create emails based on event type.
     * This replaces the need for multiple specific factory methods.
     *
     * @param to Recipient email address
     * @param event The event that triggered this email
     * @return EmailTemplate ready to be sent
     */
    fun createEmail(to: String, event: EmailEvent): EmailTemplate {
        return when (event) {
            // Recruiter and Admin events
            is EmailEvent.RecruiterInvited -> createRecruiterInvitationEmailFromEvent(to, event)
            is EmailEvent.AdminInvited -> createAdminInvitationEmailFromEvent(to, event)

            // Applicant events
            is EmailEvent.ApplicantApproved -> createApplicantApprovedEmailFromEvent(to, event)

            // Candidate events
            is EmailEvent.CandidateInvited -> createCandidateInvitationEmailFromEvent(to, event)
            is EmailEvent.ApplicantRejected -> createApplicantRejectedEmailFromEvent(to, event)
            is EmailEvent.CandidateReport -> createCandidateReportEmailFromEvent(to, event)
            is EmailEvent.CandidateAssessmentPending -> createCandidateAssessmentPendingEmailFromEvent(to, event)

            // Assessment events (for global recipients)
            is EmailEvent.AssessmentStarted -> createAssessmentStartedEmailFromEvent(to, event)
            is EmailEvent.AssessmentCompleted -> createAssessmentCompletedEmailFromEvent(to, event)
            is EmailEvent.AssessmentPending -> createAssessmentPendingEmailFromEvent(to, event)

            // Candidate application events
            is EmailEvent.CandidateApplication -> createCandidateApplicationEmailFromEvent(to, event)

            // Position events
            is EmailEvent.PositionAssigned -> createPositionAssignedEmailFromEvent(to, event)
            is EmailEvent.PositionCreated -> createPositionCreatedEmailFromEvent(to, event)
        }
    }

    /**
     * Dispatcher for global recipient emails. For events that already have dedicated
     * global-recipient templates this delegates to createEmail(). For invitation and
     * applicant-status events it routes to FYI-style templates so global recipients
     * do not receive the same personal email (with invite links, etc.) as the direct party.
     */
    fun createEmailForGlobalRecipient(to: String, event: EmailEvent): EmailTemplate {
        return when (event) {
            is EmailEvent.RecruiterInvited -> createGlobalRecipientRecruiterInvitedEmail(to, event.recruiterEmail, event.adminName)
            is EmailEvent.AdminInvited -> createGlobalRecipientAdminInvitedEmail(to, event.adminEmail, event.invitedBy)
            is EmailEvent.ApplicantApproved -> createGlobalRecipientApplicantApprovedEmail(to, event.applicantName, event.applicantEmail, event.positionTitle, event.recruiterName)
            is EmailEvent.ApplicantRejected -> createGlobalRecipientApplicantRejectedEmail(to, event.applicantName, event.applicantEmail, event.positionTitle, event.recruiterName)
            else -> createEmail(to, event)
        }
    }

    private fun createRecruiterInvitationEmailFromEvent(
        to: String,
        event: EmailEvent.RecruiterInvited
    ): EmailTemplate {
        return createRecruiterInvitationEmail(
            to = to,
            adminName = event.adminName,
            inviteLink = event.inviteLink
        )
    }

    private fun createAdminInvitationEmailFromEvent(
        to: String,
        event: EmailEvent.AdminInvited
    ): EmailTemplate {
        return createAdminInvitationEmail(
            to = to,
            invitedBy = event.invitedBy,
            inviteLink = event.inviteLink
        )
    }

    private fun createApplicantApprovedEmailFromEvent(
        to: String,
        event: EmailEvent.ApplicantApproved
    ): EmailTemplate {
        return createApplicantApprovedEmail(
            to = to,
            applicantName = event.applicantName,
            positionTitle = event.positionTitle,
            recruiterName = event.recruiterName,
        )
    }

    private fun createApplicantRejectedEmailFromEvent(
        to: String,
        event: EmailEvent.ApplicantRejected
    ): EmailTemplate {
        return createApplicantRejectedEmail(
            to = to,
            applicantName = event.applicantName,
            positionTitle = event.positionTitle,
            recruiterName = event.recruiterName
        )
    }

    private fun createCandidateInvitationEmailFromEvent(
        to: String,
        event: EmailEvent.CandidateInvited
    ): EmailTemplate {
        return createGlobalRecipientInvitationEmail(
            to = to,
            candidateEmail = event.candidateEmail,
            candidateName = event.candidateName,
            positionTitle = event.positionTitle,
            recruiterName = event.recruiterName,
            assessmentsCount = event.assessmentsCount
        )
    }

    private fun createCandidateReportEmailFromEvent(
        to: String,
        event: EmailEvent.CandidateReport
    ): EmailTemplate {
        return createCandidateReportEmail(
            to = to,
            recruiterName = event.recruiterName,
            candidateEmail = event.candidateEmail,
            candidateName = event.candidateName,
            report = event.report
        )
    }

    private fun createCandidateAssessmentPendingEmailFromEvent(
        to: String,
        event: EmailEvent.CandidateAssessmentPending
    ): EmailTemplate {
        return createCandidateAssessmentPendingEmail(
            to = to,
            recruiterName = event.recruiterName,
            candidateEmail = event.candidateEmail,
            candidateName = event.candidateName,
            assessmentId = event.assessmentId,
            timeExpired = event.timeExpired
        )
    }

    private fun createAssessmentCompletedEmailFromEvent(
        to: String,
        event: EmailEvent.AssessmentCompleted
    ): EmailTemplate {
        return createGlobalRecipientAssessmentCompletedEmail(
            to = to,
            candidateEmail = event.candidateEmail,
            candidateName = event.candidateName,
            report = event.report,
            positionTitle = event.positionTitle,
            timeExpired = event.timeExpired,
            recruiterName = event.recruiterName
        )
    }

    private fun createAssessmentPendingEmailFromEvent(
        to: String,
        event: EmailEvent.AssessmentPending
    ): EmailTemplate {
        return createGlobalRecipientAssessmentPendingEmail(
            to = to,
            candidateEmail = event.candidateEmail,
            candidateName = event.candidateName,
            assessmentId = event.assessmentId,
            positionTitle = event.positionTitle,
            timeExpired = event.timeExpired,
            recruiterName = event.recruiterName
        )
    }

    private fun createPositionAssignedEmailFromEvent(
        to: String,
        event: EmailEvent.PositionAssigned
    ): EmailTemplate {
        return createGlobalRecipientPositionAssignmentEmail(
            to = to,
            recruiterName = event.recruiterName,
            positions = event.positions,
            inviteLink = event.inviteLink
        )
    }

    private fun createPositionCreatedEmailFromEvent(
        to: String,
        event: EmailEvent.PositionCreated
    ): EmailTemplate {
        return createGlobalRecipientPositionCreationEmail(
            to = to,
            positionTitle = event.positionTitle,
            positionDescription = event.positionDescription,
            createdBy = event.createdBy,
            positionLink = event.positionLink,
            isExternal = event.isExternal,
            assessmentNames = event.assessmentNames
        )
    }

    fun createDataDeletionVerificationEmail(to: String, verificationLink: String): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Verify Your Data Deletion Request",
            textContent = """
                Hello,

                You requested deletion of your personal data.

                To confirm, click: $verificationLink

                Link expires in 24 hours.

                Best regards,
                Recruitment Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #dc3545;">Verify Your Data Deletion Request</h1>
                    <p>You requested deletion of your personal data from our recruitment system.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="$verificationLink"
                           style="background: #dc3545; color: white; padding: 12px 24px;
                                  text-decoration: none; border-radius: 4px; display: inline-block;">
                            Confirm Data Deletion
                        </a>
                    </div>
                    <p style="color: #856404; background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107;">
                        <strong>⏰ Expires in 24 hours</strong>
                    </p>
                    <p>If you did not request this, ignore this email.</p>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createDataDeletionConfirmationEmail(to: String): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Your Data Has Been Anonymized",
            textContent = """
                Hello,

                Your personal data has been successfully anonymized in our system.

                What was removed:
                - Personal information (name, email, phone)
                - CV/Resume files
                
                All personally identifiable information has been removed while preserving anonymized statistics.

                Best regards,
                Recruitment Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #28a745;">Data Anonymization Complete</h1>
                    <p>Your personal data has been successfully anonymized in our recruitment system.</p>
                    <div style="background: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0;">
                        <strong>✓ Completed:</strong>
                        <ul>
                            <li>Personal information removed</li>
                            <li>Email anonymized</li>
                            <li>Phone number deleted</li>
                            <li>CV/Resume files deleted</li>
                        </ul>
                    </div>
                    <p style="color: #6c757d; font-size: 14px;">Anonymized records are kept for statistical purposes only and cannot be linked back to you.</p>
                    <p>Best regards,<br>Recruitment Team</p>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    // ========== ADAPTER METHODS FOR NEW GLOBAL RECIPIENT EVENTS ==========

    private fun createAssessmentStartedEmailFromEvent(
        to: String,
        event: EmailEvent.AssessmentStarted
    ): EmailTemplate {
        return createGlobalRecipientAssessmentStartedEmail(
            to = to,
            candidateEmail = event.candidateEmail,
            assessmentId = event.assessmentId,
            recruiterName = event.recruiterName
        )
    }

    private fun createCandidateApplicationEmailFromEvent(
        to: String,
        event: EmailEvent.CandidateApplication
    ): EmailTemplate {
        return createGlobalRecipientCandidateApplicationEmail(
            to = to,
            candidateEmail = event.candidateEmail,
            candidateName = event.candidateName,
            positionTitle = event.positionTitle,
            recruiterName = event.recruiterName
        )
    }

    // ========== GLOBAL RECIPIENT EMAIL TEMPLATES FOR NEW EVENTS ==========

    fun createGlobalRecipientAssessmentStartedEmail(
        to: String,
        candidateEmail: String,
        assessmentId: String,
        recruiterName: String? = null
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Assessment Started: $candidateEmail",
            textContent = """
                Assessment Started

                Candidate Email: $candidateEmail
                ${recruiterName?.let { "Recruiter: $it\n" } ?: ""}Assessment ID: $assessmentId

                The candidate has started a technical assessment.

                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            color: #26a69a;
                            border-bottom: 2px solid #26a69a;
                            padding-bottom: 10px;
                        }
                        .section {
                            margin: 20px 0;
                            padding: 15px;
                            border-left: 4px solid #26a69a;
                            background-color: #f8f9fa;
                        }
                        .info-item {
                            padding: 8px 0;
                        }
                        .footer {
                            color: #666;
                            font-size: 0.9em;
                            margin-top: 20px;
                            padding-top: 10px;
                            border-top: 1px solid #ddd;
                        }
                        .icon {
                            margin-right: 8px;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="header">Assessment Started</h1>

                    <p>A candidate has started a technical assessment.</p>

                    <div class="section">
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Candidate Email:</strong> $candidateEmail
                        </div>
                        ${recruiterName?.let { """
                        <div class="info-item">
                            <span class="icon">🧑‍💼</span> <strong>Recruiter:</strong> $it
                        </div>""" } ?: ""}
                        <div class="info-item">
                            <span class="icon">📝</span> <strong>Assessment ID:</strong> $assessmentId
                        </div>
                    </div>

                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createGlobalRecipientCandidateApplicationEmail(
        to: String,
        candidateEmail: String,
        candidateName: String,
        positionTitle: String,
        recruiterName: String? = null
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "New Application: $candidateName - $positionTitle",
            textContent = """
                New Candidate Application

                Candidate: $candidateName
                Email: $candidateEmail
                Position: $positionTitle
                ${recruiterName?.let { "Recruiter: $it\n" } ?: ""}
                A new candidate has applied for a position.

                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            color: #26a69a;
                            border-bottom: 2px solid #26a69a;
                            padding-bottom: 10px;
                        }
                        .section {
                            margin: 20px 0;
                            padding: 15px;
                            border-left: 4px solid #26a69a;
                            background-color: #f8f9fa;
                        }
                        .info-item {
                            padding: 8px 0;
                        }
                        .footer {
                            color: #666;
                            font-size: 0.9em;
                            margin-top: 20px;
                            padding-top: 10px;
                            border-top: 1px solid #ddd;
                        }
                        .icon {
                            margin-right: 8px;
                        }
                    </style>
                </head>
                <body>
                    <h1 class="header">New Candidate Application</h1>

                    <p>A new candidate has applied for a position.</p>

                    <div class="section">
                        <div class="info-item">
                            <span class="icon">👤</span> <strong>Candidate:</strong> $candidateName
                        </div>
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Email:</strong> $candidateEmail
                        </div>
                        <div class="info-item">
                            <span class="icon">💼</span> <strong>Position:</strong> $positionTitle
                        </div>
                        ${recruiterName?.let { """
                        <div class="info-item">
                            <span class="icon">🧑‍💼</span> <strong>Recruiter:</strong> $it
                        </div>""" } ?: ""}
                    </div>

                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createCandidateApplicationConfirmationEmail(
        to: String,
        candidateName: String,
        positionTitle: String
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Application Received - $positionTitle",
            textContent = """
                Hello
                
                Thank you for applying to the $positionTitle position.
                
                We have successfully received your application and our team will review it shortly.
                
                
                Thank you for your interest in joining our team!
                
                Best regards,
                Recruitment Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            background: white;
                            border-radius: 12px;
                            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #26a69a 0%, #1de9b6 100%);
                            color: white;
                            padding: 40px 30px;
                            text-align: center;
                        }
                        .header h1 {
                            font-size: 28px;
                            font-weight: 600;
                            margin: 0 0 10px 0;
                        }
                        .header p {
                            font-size: 16px;
                            margin: 0;
                            opacity: 0.95;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .success-badge {
                            display: inline-block;
                            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                            color: white;
                            padding: 8px 20px;
                            border-radius: 20px;
                            font-size: 14px;
                            font-weight: 600;
                            margin-bottom: 20px;
                        }
                        .position-box {
                            background: linear-gradient(135deg, #e0f7f4 0%, #f0fffe 100%);
                            padding: 20px;
                            border-radius: 8px;
                            margin: 24px 0;
                            border-left: 4px solid #26a69a;
                        }
                        .position-box h3 {
                            color: #2c3e50;
                            font-size: 18px;
                            margin: 0 0 8px 0;
                        }
                        .position-box p {
                            color: #4b5563;
                            margin: 0;
                            font-size: 16px;
                        }
                        .footer {
                            color: #666;
                            font-size: 14px;
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #e0e0e0;
                            text-align: center;
                        }
                        .footer strong {
                            color: #2c3e50;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Application Received!</h1>
                            <p>Thank you for your interest</p>
                        </div>
                        
                        <div class="content">
                            <div class="success-badge">✓ SUCCESSFULLY SUBMITTED</div>
                            
                            <p>Hello <strong>$candidateName</strong>!</p>
                            
                            <div class="position-box">
                                <h3>📋 Position Applied</h3>
                                <p>$positionTitle</p>
                            </div>
                            
                            <p>We have successfully received your application and our team will review it shortly.</p>
                            
                            <p>Thank you for your interest in joining our team!</p>
                            
                            <div class="footer">
                                <p>Best regards,<br><strong>Recruitment Team</strong></p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createDataDownloadVerificationEmail(to: String, downloadLink: String): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Your Data Download Link",
            textContent = """
                Hello,

                You requested a copy of your personal data.

                To download, click: $downloadLink

                Link expires in 24 hours.

                Best regards,
                Recruitment Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h1 style="color: #007bff;">Your Data Download Link</h1>
                    <p>You requested a copy of your personal data from our recruitment system.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="$downloadLink"
                           style="background: #007bff; color: white; padding: 12px 24px;
                                  text-decoration: none; border-radius: 4px; display: inline-block;">
                            Download My Data
                        </a>
                    </div>
                    <p style="color: #856404; background: #fff3cd; padding: 15px; border-left: 4px solid #ffc107;">
                        <strong>⏰ Expires in 24 hours</strong>
                    </p>
                    <p>The download will be in JSON format containing all your application data.</p>
                    <p>If you did not request this, ignore this email.</p>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createRecruiterCandidateInvitationConfirmationEmail(
        to: String,
        recruiterName: String,
        candidateName: String,
        candidateEmail: String,
        positionTitle: String,
        assessmentsCount: Int
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Candidate Invitation Sent - $candidateName",
            textContent = """
                Hello $recruiterName!
                
                You have successfully invited a candidate to the $positionTitle position.
                
                Candidate Details:
                • Name: $candidateName
                • Email: $candidateEmail
                • Assessments: $assessmentsCount
                
                The candidate has been sent an email with instructions to complete the assessment(s).
                
                You will receive notifications when:
                • The candidate starts the assessment
                • The candidate completes the assessment
                • Assessment results are available
                
                Best regards,
                Recruitment System
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f5f5f5;
                        }
                        .container {
                            background: white;
                            border-radius: 12px;
                            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
                            color: white;
                            padding: 40px 30px;
                            text-align: center;
                        }
                        .header h1 {
                            font-size: 28px;
                            font-weight: 600;
                            margin: 0 0 10px 0;
                        }
                        .header p {
                            font-size: 16px;
                            margin: 0;
                            opacity: 0.95;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .success-badge {
                            display: inline-block;
                            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                            color: white;
                            padding: 8px 20px;
                            border-radius: 20px;
                            font-size: 14px;
                            font-weight: 600;
                            margin-bottom: 20px;
                        }
                        .info-box {
                            background: linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%);
                            padding: 20px;
                            border-radius: 8px;
                            margin: 24px 0;
                            border-left: 4px solid #3b82f6;
                        }
                        .info-box h3 {
                            color: #2c3e50;
                            font-size: 18px;
                            margin: 0 0 16px 0;
                        }
                        .info-item {
                            padding: 8px 0;
                            display: flex;
                            align-items: flex-start;
                        }
                        .info-icon {
                            color: #3b82f6;
                            margin-right: 12px;
                            font-size: 18px;
                            flex-shrink: 0;
                        }
                        .info-text {
                            color: #4b5563;
                            font-size: 15px;
                        }
                        .next-steps {
                            background: #f8f9fa;
                            padding: 24px;
                            border-radius: 8px;
                            margin: 24px 0;
                        }
                        .next-steps h3 {
                            color: #2c3e50;
                            font-size: 18px;
                            margin: 0 0 16px 0;
                        }
                        .step-item {
                            padding: 12px 0;
                            border-bottom: 1px solid #e0e0e0;
                            display: flex;
                            align-items: flex-start;
                        }
                        .step-item:last-child {
                            border-bottom: none;
                        }
                        .step-icon {
                            color: #10b981;
                            margin-right: 12px;
                            font-size: 18px;
                            flex-shrink: 0;
                        }
                        .step-text {
                            color: #4b5563;
                            font-size: 15px;
                        }
                        .footer {
                            color: #666;
                            font-size: 14px;
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #e0e0e0;
                            text-align: center;
                        }
                        .footer strong {
                            color: #2c3e50;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>✉️ Invitation Sent!</h1>
                            <p>Candidate has been invited</p>
                        </div>
                        
                        <div class="content">
                            <div class="success-badge">✓ SUCCESSFULLY SENT</div>
                            
                            <p>Hello <strong>$recruiterName</strong>!</p>
                            
                            <p>You have successfully invited a candidate to the <strong>$positionTitle</strong> position.</p>
                            
                            <div class="info-box">
                                <h3>👤 Candidate Details</h3>
                                <div class="info-item">
                                    <span class="info-icon">•</span>
                                    <span class="info-text"><strong>Name:</strong> $candidateName</span>
                                </div>
                                <div class="info-item">
                                    <span class="info-icon">•</span>
                                    <span class="info-text"><strong>Email:</strong> $candidateEmail</span>
                                </div>
                                <div class="info-item">
                                    <span class="info-icon">•</span>
                                    <span class="info-text"><strong>Assessments:</strong> $assessmentsCount</span>
                                </div>
                            </div>
                            
                            <p>The candidate has been sent an email with instructions to complete the assessment(s).</p>
                            
                            <div class="next-steps">
                                <h3>📬 You will receive notifications when:</h3>
                                <div class="step-item">
                                    <span class="step-icon">✓</span>
                                    <span class="step-text">The candidate starts the assessment</span>
                                </div>
                                <div class="step-item">
                                    <span class="step-icon">✓</span>
                                    <span class="step-text">The candidate completes the assessment</span>
                                </div>
                                <div class="step-item">
                                    <span class="step-icon">✓</span>
                                    <span class="step-text">Assessment results are available</span>
                                </div>
                            </div>
                            
                            <div class="footer">
                                <p>Best regards,<br><strong>Recruitment System</strong></p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    // ========== GLOBAL RECIPIENT FYI TEMPLATES FOR INVITATION AND STATUS EVENTS ==========

    fun createGlobalRecipientRecruiterInvitedEmail(
        to: String,
        recruiterEmail: String,
        adminName: String?
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Recruiter Invited: $recruiterEmail",
            textContent = """
                Recruiter Invited

                A new recruiter has been invited to the platform.

                Recruiter Email: $recruiterEmail
                ${adminName?.let { "Invited By: $it\n" } ?: ""}
                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { color: #26a69a; border-bottom: 2px solid #26a69a; padding-bottom: 10px; }
                        .section { margin: 20px 0; padding: 15px; border-left: 4px solid #26a69a; background-color: #f8f9fa; }
                        .info-item { padding: 8px 0; }
                        .footer { color: #666; font-size: 0.9em; margin-top: 20px; padding-top: 10px; border-top: 1px solid #ddd; }
                        .icon { margin-right: 8px; }
                    </style>
                </head>
                <body>
                    <h1 class="header">Recruiter Invited</h1>
                    <p>A new recruiter has been invited to the platform.</p>
                    <div class="section">
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Recruiter Email:</strong> $recruiterEmail
                        </div>
                        ${adminName?.let { """
                        <div class="info-item">
                            <span class="icon">🧑‍💼</span> <strong>Invited By:</strong> $it
                        </div>""" } ?: ""}
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createGlobalRecipientAdminInvitedEmail(
        to: String,
        adminEmail: String,
        invitedBy: String?
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Admin Invited: $adminEmail",
            textContent = """
                Admin Invited

                A new admin has been invited to the platform.

                Admin Email: $adminEmail
                ${invitedBy?.let { "Invited By: $it\n" } ?: ""}
                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { color: #26a69a; border-bottom: 2px solid #26a69a; padding-bottom: 10px; }
                        .section { margin: 20px 0; padding: 15px; border-left: 4px solid #26a69a; background-color: #f8f9fa; }
                        .info-item { padding: 8px 0; }
                        .footer { color: #666; font-size: 0.9em; margin-top: 20px; padding-top: 10px; border-top: 1px solid #ddd; }
                        .icon { margin-right: 8px; }
                    </style>
                </head>
                <body>
                    <h1 class="header">Admin Invited</h1>
                    <p>A new admin has been invited to the platform.</p>
                    <div class="section">
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Admin Email:</strong> $adminEmail
                        </div>
                        ${invitedBy?.let { """
                        <div class="info-item">
                            <span class="icon">🧑‍💼</span> <strong>Invited By:</strong> $it
                        </div>""" } ?: ""}
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createGlobalRecipientApplicantApprovedEmail(
        to: String,
        applicantName: String?,
        applicantEmail: String,
        positionTitle: String,
        reviewedBy: String?
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Applicant Approved: ${applicantName ?: applicantEmail} - $positionTitle",
            textContent = """
                Applicant Approved

                An applicant has been approved for a position.

                Applicant: ${applicantName ?: applicantEmail}
                Email: $applicantEmail
                Position: $positionTitle
                ${reviewedBy?.let { "Reviewed By: $it\n" } ?: ""}
                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { color: #26a69a; border-bottom: 2px solid #26a69a; padding-bottom: 10px; }
                        .section { margin: 20px 0; padding: 15px; border-left: 4px solid #26a69a; background-color: #f8f9fa; }
                        .info-item { padding: 8px 0; }
                        .footer { color: #666; font-size: 0.9em; margin-top: 20px; padding-top: 10px; border-top: 1px solid #ddd; }
                        .icon { margin-right: 8px; }
                    </style>
                </head>
                <body>
                    <h1 class="header">Applicant Approved</h1>
                    <p>An applicant has been approved for a position.</p>
                    <div class="section">
                        <div class="info-item">
                            <span class="icon">👤</span> <strong>Applicant:</strong> ${applicantName ?: applicantEmail}
                        </div>
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Email:</strong> $applicantEmail
                        </div>
                        <div class="info-item">
                            <span class="icon">💼</span> <strong>Position:</strong> $positionTitle
                        </div>
                        ${reviewedBy?.let { """
                        <div class="info-item">
                            <span class="icon">🧑‍💼</span> <strong>Reviewed By:</strong> $it
                        </div>""" } ?: ""}
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }

    fun createGlobalRecipientApplicantRejectedEmail(
        to: String,
        applicantName: String?,
        applicantEmail: String,
        positionTitle: String,
        reviewedBy: String?
    ): EmailTemplate {
        return EmailTemplate(
            to = to,
            subject = "Applicant Rejected: ${applicantName ?: applicantEmail} - $positionTitle",
            textContent = """
                Applicant Rejected

                An applicant has been rejected for a position.

                Applicant: ${applicantName ?: applicantEmail}
                Email: $applicantEmail
                Position: $positionTitle
                ${reviewedBy?.let { "Reviewed By: $it\n" } ?: ""}
                ---
                Score-pion Team
            """.trimIndent(),
            htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { color: #e53935; border-bottom: 2px solid #e53935; padding-bottom: 10px; }
                        .section { margin: 20px 0; padding: 15px; border-left: 4px solid #e53935; background-color: #f8f9fa; }
                        .info-item { padding: 8px 0; }
                        .footer { color: #666; font-size: 0.9em; margin-top: 20px; padding-top: 10px; border-top: 1px solid #ddd; }
                        .icon { margin-right: 8px; }
                    </style>
                </head>
                <body>
                    <h1 class="header">Applicant Rejected</h1>
                    <p>An applicant has been rejected for a position.</p>
                    <div class="section">
                        <div class="info-item">
                            <span class="icon">👤</span> <strong>Applicant:</strong> ${applicantName ?: applicantEmail}
                        </div>
                        <div class="info-item">
                            <span class="icon">📧</span> <strong>Email:</strong> $applicantEmail
                        </div>
                        <div class="info-item">
                            <span class="icon">💼</span> <strong>Position:</strong> $positionTitle
                        </div>
                        ${reviewedBy?.let { """
                        <div class="info-item">
                            <span class="icon">🧑‍💼</span> <strong>Reviewed By:</strong> $it
                        </div>""" } ?: ""}
                    </div>
                    <div class="footer">
                        <p>Best regards,<br><strong>Score-pion Team</strong></p>
                    </div>
                </body>
                </html>
            """.trimIndent(),
            from = defaultFrom
        )
    }
}

