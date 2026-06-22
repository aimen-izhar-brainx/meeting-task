package com.example.meetingtask.ai.service

import com.example.meetingtask.ai.mapper.PromptBuilder
import com.example.meetingtask.data.mapper.AiTicketMapper
import com.example.meetingtask.data.model.openai.OpenAiChatRequestDto
import com.example.meetingtask.data.model.openai.OpenAiChatResponseDto
import com.example.meetingtask.data.model.openai.OpenAiMessageDto
import com.example.meetingtask.domain.model.JiraImportTicket
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class OpenAiTicketGenerator(
    private val apiKey: String,
    private val promptBuilder: PromptBuilder = PromptBuilder()
) : AiTicketGenerator {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    override fun generateTickets(rawClientBrief: String): List<JiraImportTicket> {
        require(apiKey.isNotBlank()) { "OPENAI_API_KEY is missing. Add it to local.properties." }

        val requestPayload = OpenAiChatRequestDto(
            model = OpenAiConstants.DEFAULT_MODEL,
            messages = listOf(
                OpenAiMessageDto(
                    role = OpenAiConstants.ROLE_SYSTEM,
                    content = OpenAiConstants.SYSTEM_PROMPT
                ),
                OpenAiMessageDto(
                    role = OpenAiConstants.ROLE_USER,
                    content = promptBuilder.buildPrompt(rawClientBrief)
                )
            )
        )

        val connection = URL(OpenAiConstants.CHAT_COMPLETIONS_URL)
            .openConnection()
            .let { connection ->
                connection as? HttpURLConnection
                    ?: throw IllegalStateException("Expected HTTP connection for OpenAI API request.")
            }
            .apply {
                requestMethod = OpenAiConstants.HTTP_METHOD_POST
                connectTimeout = OpenAiConstants.CONNECT_TIMEOUT_MS
                readTimeout = OpenAiConstants.READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty(
                    OpenAiConstants.HEADER_AUTHORIZATION,
                    "${OpenAiConstants.AUTHORIZATION_BEARER_PREFIX}$apiKey"
                )
                setRequestProperty(OpenAiConstants.HEADER_CONTENT_TYPE, OpenAiConstants.CONTENT_TYPE_JSON)
            }

        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(json.encodeToString(OpenAiChatRequestDto.serializer(), requestPayload))
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(
                connection,
                statusCode in OpenAiConstants.HTTP_SUCCESS_MIN..OpenAiConstants.HTTP_SUCCESS_MAX
            )
            if (statusCode !in OpenAiConstants.HTTP_SUCCESS_MIN..OpenAiConstants.HTTP_SUCCESS_MAX) {
                throw IllegalStateException("OpenAI API error ($statusCode): $responseText")
            }

            return AiTicketMapper.parseTicketResponse(extractMessageContent(responseText))
        } finally {
            connection.disconnect()
        }
    }

    private fun extractMessageContent(responseText: String): String {
        val response = json.decodeFromString(OpenAiChatResponseDto.serializer(), responseText)
        val choices = response.choices
        if (choices.isEmpty()) {
            throw IllegalStateException("OpenAI response contains no choices.")
        }

        val content = choices.first().message?.content?.trim().orEmpty()
        if (content.isBlank()) {
            throw IllegalStateException("OpenAI response is missing message content.")
        }
        return content
    }

    private fun readResponse(connection: HttpURLConnection, success: Boolean): String {
        val stream = if (success) connection.inputStream else connection.errorStream
        if (stream == null) return ""
        return BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
            buildString {
                var line = reader.readLine()
                while (line != null) {
                    append(line)
                    line = reader.readLine()
                }
            }
        }
    }
}
