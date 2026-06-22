package com.example.meetingtask.ai.service

import com.example.meetingtask.domain.model.JiraImportTicket

interface AiTicketGenerator {
    fun generateTickets(rawClientBrief: String): List<JiraImportTicket>
}
