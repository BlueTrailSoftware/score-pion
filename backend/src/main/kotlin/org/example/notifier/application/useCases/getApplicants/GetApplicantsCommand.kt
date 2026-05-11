package org.example.notifier.application.useCases.getApplicants

import org.example.notifier.application.model.shared.PageQuery

data class GetApplicantsCommand(
    val currentUserId: String,
    val isAdmin: Boolean,
    val status: String?,
    val positionId: String?,
    val search: String?,
    val pageQuery: PageQuery = PageQuery()
)