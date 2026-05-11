package org.example.notifier.application.model.shared

data class PagedResult<T>(
    val items: List<T>,
    val total: Int
)

fun <T> List<T>.toPagedResult(page: Int, pageSize: Int): PagedResult<T> =
    PagedResult(items = drop(page * pageSize).take(pageSize), total = size)

fun <T, R> PagedResult<T>.map(transform: (T) -> R): PagedResult<R> =
    PagedResult(items = items.map(transform), total = total)