package org.example.notifier.infrastructure.adapter.coderbyte.mapper

import org.example.notifier.domain.shared.AssessmentReport
import org.example.notifier.domain.shared.CheatingInfo
import org.example.notifier.domain.shared.MultipleChoiceDetail
import org.example.notifier.infrastructure.external.Report

object CoderbyteReportMapper {

    fun toDomainReport(
        coderbyteReport: Report,
        candidateEmail: String,
        assessmentId: String
    ): AssessmentReport {
        return AssessmentReport(
            candidateEmail = candidateEmail,
            assessmentId = assessmentId,
            displayName = coderbyteReport.displayName.orEmpty(),

            finalScore = coderbyteReport.finalScore?.toDouble() ?: 0.0,
            mcScore = coderbyteReport.mcScore,
            codeScore = coderbyteReport.codeScore,
            qualifyingScore = coderbyteReport.qualifyingScore,

            status = coderbyteReport.status.orEmpty(),
            isQualified = coderbyteReport.qualified ?: false,

            mcDetails = coderbyteReport.mcDetails?.map { mcDetail ->
                MultipleChoiceDetail(
                    id = mcDetail.id,
                    question = mcDetail.question,
                    correct = mcDetail.correct,
                    answer = mcDetail.answer,
                    tags = mcDetail.meta?.tags
                )
            },

            timeTaken = coderbyteReport.timeTaken,
            reportLink = coderbyteReport.reportLink,
            workspaces = coderbyteReport.workspaces,

            cheatingDetails = coderbyteReport.cheatingDetails?.let {
                CheatingInfo(
                    tabLeaving = it.tabLeaving,
                    plagiarism = it.plagiarism,
                    pastedCode = it.pastedCode,
                    suspiciousActivity = it.suspiciousActivity,
                    aiUsage = it.aiUsage
                )
            },

            metadata = mapOf(
                "provider" to "coderbyte",
                "username" to (coderbyteReport.username ?: ""),
                "dateJoined" to (coderbyteReport.dateJoined ?: ""),
                "testId" to (coderbyteReport.testId ?: ""),
                "totalChallenges" to (coderbyteReport.totalChallenges ?: 0),
                "reportReady" to (coderbyteReport.reportReady ?: false),
                "cheatingFlag" to (coderbyteReport.cheatingFlag ?: "")
            )
        )
    }
}
