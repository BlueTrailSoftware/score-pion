package org.example.notifier.infrastructure.external

import com.fasterxml.jackson.annotation.JsonProperty

// DTOs for Asana API
data class AsanaTaskRequest(
    val data: AsanaTaskData
)

data class AsanaTaskData(
    val name: String,
    val notes: String? = null,
    val projects: List<String>
)

data class AsanaTaskResponse(
    val data: AsanaTask
)

data class AsanaTask(
    val gid: String,
    val name: String,
    @JsonProperty("permalink_url")
    val permalinkUrl: String
)
