package com.example.meetingtask.data.repository

import com.example.meetingtask.ai.service.AiTicketGenerator
import com.example.meetingtask.data.mapper.JiraTicketCsvMapper
import com.example.meetingtask.domain.model.JiraTicketDraft
import com.example.meetingtask.domain.repository.ClientBriefRepository

class ClientBriefRepositoryImpl(
    private val aiTicketGenerator: AiTicketGenerator
) : ClientBriefRepository {

    override fun generateTicketFromBrief(clientBrief: String): JiraTicketDraft {
        val tickets = aiTicketGenerator.generateTickets(clientBrief)
        return JiraTicketDraft(
            tickets = tickets,
            csvContent = JiraTicketCsvMapper.toCsv(tickets)
        )
    }
}
