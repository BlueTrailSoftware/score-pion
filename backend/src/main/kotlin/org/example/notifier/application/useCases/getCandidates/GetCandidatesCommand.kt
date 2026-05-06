package org.example.notifier.application.useCases.getCandidates

import org.example.notifier.application.model.shared.PageQuery

data class GetCandidatesCommand(
    val recruiterId: String?,
    val search: String? = null,
    val pageQuery: PageQuery = PageQuery()
)