package org.example.notifier.application.useCases.requestDataExport

sealed class RequestDataExportResult {
    object NoApplicantFound : RequestDataExportResult()
    object DownloadEmailSent : RequestDataExportResult()
}
