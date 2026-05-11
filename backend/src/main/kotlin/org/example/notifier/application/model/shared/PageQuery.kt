package org.example.notifier.application.model.shared

data class PageQuery(
    val page: Int = 0,
    val pageSize: Int = 10,
    val sortField: String? = null,
    val sortDirection: SortDirection = SortDirection.DESC
)