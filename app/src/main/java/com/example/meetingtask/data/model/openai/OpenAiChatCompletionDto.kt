package com.example.meetingtask.data.model.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiChatRequestDto(
    val model: String,
    @SerialName("response_format") val responseFormat: OpenAiResponseFormatDto = OpenAiResponseFormatDto(),
    val messages: List<OpenAiMessageDto> = emptyList()
)

@Serializable
data class OpenAiResponseFormatDto(
    val type: String = "json_object"
)

@Serializable
data class OpenAiMessageDto(
    val role: String,
    val content: String
)

@Serializable
data class OpenAiChatResponseDto(
    val choices: List<OpenAiChoiceDto> = emptyList()
)

@Serializable
data class OpenAiChoiceDto(
    val message: OpenAiMessageDto? = null
)
