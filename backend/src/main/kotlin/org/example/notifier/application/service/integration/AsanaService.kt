package org.example.notifier.application.service.integration

import kotlinx.coroutines.reactive.awaitSingle
import org.example.notifier.infrastructure.config.AsanaProperties
import org.example.notifier.infrastructure.dto.request.CreateTicketRequest
import org.example.notifier.infrastructure.dto.response.CreateTicketResponse
import org.example.notifier.infrastructure.external.AsanaTaskRequest
import org.example.notifier.infrastructure.external.AsanaTaskResponse
import org.example.notifier.infrastructure.external.AsanaTaskData
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import kotlin.RuntimeException

@Service
class AsanaService(private val asanaProperties: AsanaProperties) {

    private val webClient = WebClient.builder()
        .baseUrl(asanaProperties.apiUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${asanaProperties.token}")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build()


    suspend fun createTask(request: CreateTicketRequest, userEmail: String): CreateTicketResponse {

            val formattedNotes = """
                
            Assessment request details
            ==================================
            
            📧 Email address
            $userEmail
            
            📅 Target completion date
            ${request.readyDate}
            
            📝 Description
            ${request.description}
            
            
            ==================================
            Created via Score-Pion App
    
        """.trimIndent()

        val asanaRequest = AsanaTaskRequest(
            data = AsanaTaskData(
                name = "Candidates Evaluation Request Exam Creation submission",
                notes = formattedNotes,
                projects = listOf(asanaProperties.projectId)
            )
        )

        val response = webClient.post()
            .uri("/tasks")
            .bodyValue(asanaRequest)
            .retrieve()
            .awaitBody<AsanaTaskResponse>()

        return CreateTicketResponse(
            success = true,
            taskId = response.data.gid,
            taskUrl = response.data.permalinkUrl,
            message = "Task created successfully in Asana"
        )
    }
}
