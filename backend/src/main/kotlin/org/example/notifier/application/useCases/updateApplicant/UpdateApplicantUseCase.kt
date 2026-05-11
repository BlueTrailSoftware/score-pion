package org.example.notifier.application.useCases.updateApplicant

import org.example.notifier.application.model.applicant.ApplicantItem
import org.example.notifier.application.model.applicant.toApplicantItem
import org.example.notifier.application.service.core.ApplicantService
import org.example.notifier.application.service.core.OpenPositionService
import org.example.notifier.application.service.file.FileService
import org.springframework.stereotype.Component

@Component
class UpdateApplicantUseCase(
    private val applicantService: ApplicantService,
    private val openPositionService: OpenPositionService,
    private val fileService: FileService
) {

    suspend fun execute(command: UpdateApplicantCommand): ApplicantItem {
        val currentApplicant = applicantService.getApplicantById(command.id)
            ?: throw IllegalArgumentException("Applicant not found with id: ${command.id}")

        val fileResult = fileService.handleFileUpdate(
            currentFileUrl = currentApplicant.fileUrl,
            newFilePart = command.filePart,
            deleteFile = command.deleteFile,
            entityType = "candidates",
            entityId = command.id
        )

        val applicant = applicantService.updateApplicant(
            id = command.id,
            name = command.name,
            email = command.email,
            phone = command.phone,
            fileUrl = fileResult.fileUrl,
            isFileDeleted = fileResult.isFileDeleted
        )
        val position = openPositionService.getPosition(applicant.positionId)
        return applicant.toApplicantItem(position?.title)
    }
}