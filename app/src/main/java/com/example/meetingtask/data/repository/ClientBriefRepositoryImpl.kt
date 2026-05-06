package com.example.meetingtask.data.repository

import com.example.meetingtask.ai.service.AiTicketGenerator
import com.example.meetingtask.domain.model.JiraImportTicket
import com.example.meetingtask.domain.model.JiraTicketDraft
import com.example.meetingtask.domain.repository.ClientBriefRepository
import org.json.JSONObject

class ClientBriefRepositoryImpl(
    private val aiTicketGenerator: AiTicketGenerator
) : ClientBriefRepository {

    override fun generateTicketFromBrief(clientBrief: String): JiraTicketDraft {
        val jsonOutput = aiTicketGenerator.generateTicketJson(clientBrief)
        val root = JSONObject(jsonOutput)
        val ticketsJson = root.getJSONArray("tickets")

        val tickets = List(ticketsJson.length()) { index ->
            val ticket = ticketsJson.getJSONObject(index)
            JiraImportTicket(
                summary = ticket.optString("summary", "NEEDS CLARIFICATION"),
                issueType = ticket.optString("issue_type", "Task"),
                description = ticket.optString("description", "NEEDS CLARIFICATION"),
                epicName = ticket.optString("epic_name", ""),
                parentSummary = ticket.optString("parent_summary", "")
            )
        }

        return JiraTicketDraft(
            tickets = tickets,
            csvContent = toJiraCsv(tickets)
        )
    }

    private fun toJiraCsv(tickets: List<JiraImportTicket>): String {
        val header = "Summary,Issue Type,Description,Epic Name,Parent Summary"
        if (tickets.isEmpty()) return header

        val rows = tickets.joinToString("\n") { ticket ->
            listOf(
                escapeCsv(ticket.summary),
                escapeCsv(ticket.issueType),
                escapeCsv(ticket.description),
                escapeCsv(ticket.epicName),
                escapeCsv(ticket.parentSummary)
            ).joinToString(",")
        }
        return "$header\n$rows"
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
