package com.example.meetingtask.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiTicketResponseDto(
    val tickets: List<AiTicketItemDto> = emptyList()
)

@Serializable
data class AiTicketItemDto(
    val summary: String? = null,
    @SerialName("issue_type") val issueType: String? = null,
    val description: String? = null,
    @SerialName("epic_name") val epicName: String? = null,
    @SerialName("parent_summary") val parentSummary: String? = null
)
