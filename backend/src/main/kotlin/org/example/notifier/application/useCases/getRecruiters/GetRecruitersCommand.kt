package org.example.notifier.application.useCases.getRecruiters

data class GetRecruitersCommand(
    val page: Int = 0,
    val pageSize: Int = 10
)