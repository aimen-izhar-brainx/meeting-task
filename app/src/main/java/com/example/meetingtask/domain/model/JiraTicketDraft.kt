package com.example.meetingtask.domain.model

data class JiraTicketDraft(
    val tickets: List<JiraImportTicket>,
    val csvContent: String
)
