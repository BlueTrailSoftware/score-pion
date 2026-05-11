package org.example.notifier.infrastructure.dto.response

import kotlin.math.ceil
import org.example.notifier.application.model.shared.PageQuery
import org.example.notifier.application.model.shared.PagedResult

data class PagedData<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
) {
    companion object {
        fun <T> of(items: List<T>, total: Int, page: Int, pageSize: Int): PagedData<T> =
            PagedData(items, total, page, pageSize, if (total == 0) 0 else ceil(total.toDouble() / pageSize).toInt())
    }
}

fun <T> PagedResult<T>.toPagedData(pageQuery: PageQuery): PagedData<T> =
    PagedData.of(items, total, pageQuery.page, pageQuery.pageSize)

fun <T> PagedResult<T>.toPagedData(page: Int, pageSize: Int): PagedData<T> =
    PagedData.of(items, total, page, pageSize)