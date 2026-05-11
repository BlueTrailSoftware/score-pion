package org.example.notifier.application.model.shared

fun <T> List<T>.applySortAndPage(
    pageQuery: PageQuery,
    comparators: Map<String, Comparator<T>>,
    default: Comparator<T>
): PagedResult<T> {
    val comparator = if (pageQuery.sortField != null) {
        val base = comparators[pageQuery.sortField] ?: default
        if (pageQuery.sortDirection == SortDirection.ASC) base else base.reversed()
    } else {
        default
    }
    return sortedWith(comparator).toPagedResult(pageQuery.page, pageQuery.pageSize)
}