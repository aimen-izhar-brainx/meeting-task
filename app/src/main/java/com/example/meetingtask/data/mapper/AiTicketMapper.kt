package com.example.meetingtask.data.mapper

import com.example.meetingtask.data.model.AiTicketItemDto
import com.example.meetingtask.data.model.AiTicketResponseDto
import com.example.meetingtask.domain.model.JiraImportTicket
import com.example.meetingtask.domain.model.JiraIssueType
import kotlinx.serialization.json.Json

object AiTicketMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun parseTicketResponse(rawJson: String): List<JiraImportTicket> {
        val cleaned = rawJson
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val response = json.decodeFromString(AiTicketResponseDto.serializer(), cleaned)
        return response.tickets.map { it.toDomain() }
    }

    private fun AiTicketItemDto.toDomain(): JiraImportTicket {
        return JiraImportTicket(
            summary = summary.orDefault("NEEDS CLARIFICATION"),
            issueType = JiraIssueType.fromRaw(issueType),
            description = description.orDefault("NEEDS CLARIFICATION"),
            epicName = epicName.orDefault(),
            parentSummary = parentSummary.orDefault()
        )
    }

    private fun String?.orDefault(default: String = ""): String {
        val value = this?.trim().orEmpty()
        return value.ifBlank { default }
    }
}
