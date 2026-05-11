package org.example.notifier.infrastructure.util

const val MAX_PAGE_SIZE = 50

fun Int.cappedPageSize(): Int = coerceAtMost(MAX_PAGE_SIZE)