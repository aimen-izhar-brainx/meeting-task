package com.example.meetingtask.ai.service

import com.example.meetingtask.ai.mapper.PromptBuilder
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class OpenAiTicketGenerator(
    private val apiKey: String,
    private val promptBuilder: PromptBuilder = PromptBuilder()
) : AiTicketGenerator {

    override fun generateTicketJson(rawClientBrief: String): String {
        require(apiKey.isNotBlank()) { "OPENAI_API_KEY is missing. Add it to local.properties." }

        val requestPayload = JSONObject()
            .put("model", "gpt-4.1-mini")
            .put("response_format", JSONObject().put("type", "json_object"))
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                "content",
                                "Return only JSON for Jira CSV import. Include tickets array with strict fields: summary, issue_type, description, epic_name, parent_summary."
                            )
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", promptBuilder.buildPrompt(rawClientBrief))
                    )
            )

        val connection = (URL("https://api.openai.com/v1/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
        }

        try {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(requestPayload.toString())
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection, statusCode in 200..299)
            if (statusCode !in 200..299) {
                throw IllegalStateException("OpenAI API error ($statusCode): $responseText")
            }

            val root = JSONObject(responseText)
            val content = root
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            return normalizeTicketJson(content)
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection, success: Boolean): String {
        val stream = if (success) connection.inputStream else connection.errorStream
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

    private fun normalizeTicketJson(content: String): String {
        val cleaned = content
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val parsed = JSONObject(cleaned)
        val ticketsRaw = parsed.optJSONArray("tickets") ?: JSONArray()
        val normalizedTickets = JSONArray()

        for (i in 0 until ticketsRaw.length()) {
            val raw = ticketsRaw.optJSONObject(i) ?: JSONObject()
            val issueType = normalizeIssueType(raw.optString("issue_type", "Task"))
            normalizedTickets.put(
                JSONObject()
                    .put("summary", raw.optString("summary", "NEEDS CLARIFICATION"))
                    .put("issue_type", issueType)
                    .put("description", raw.optString("description", "NEEDS CLARIFICATION"))
                    .put("epic_name", raw.optString("epic_name", ""))
                    .put("parent_summary", raw.optString("parent_summary", ""))
            )
        }

        return JSONObject().put("tickets", normalizedTickets).toString()
    }

    private fun normalizeIssueType(issueType: String): String {
        return when (issueType.trim().lowercase()) {
            "epic" -> "Epic"
            "story", "user story", "user_story" -> "Story"
            else -> "Task"
        }
    }
}
