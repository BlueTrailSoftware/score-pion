package org.example.notifier.application.useCases.getAllAdmins

data class GetAllAdminsCommand(
    val page: Int = 0,
    val pageSize: Int = 10
)