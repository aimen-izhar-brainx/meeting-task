package com.example.meetingtask.data.mapper

import com.example.meetingtask.domain.model.JiraImportTicket

object JiraTicketCsvMapper {

    fun toCsv(tickets: List<JiraImportTicket>): String {
        val header = "Summary,Issue Type,Description,Epic Name,Parent Summary"
        if (tickets.isEmpty()) return header

        val rows = tickets.joinToString("\n") { ticket ->
            listOf(
                escapeCsv(ticket.summary),
                escapeCsv(ticket.issueType.label),
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
