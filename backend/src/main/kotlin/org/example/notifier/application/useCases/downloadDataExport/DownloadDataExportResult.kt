package org.example.notifier.application.useCases.downloadDataExport

sealed class DownloadDataExportResult {
    data class Success(val data: ByteArray, val filename: String) : DownloadDataExportResult()
    object InvalidToken : DownloadDataExportResult()
    object DataNotAvailable : DownloadDataExportResult()
}
