package org.example.notifier.application.useCases.updateApplicant

import org.springframework.http.codec.multipart.FilePart

data class UpdateApplicantCommand(
    val id: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val filePart: FilePart?,
    val deleteFile: Boolean
)