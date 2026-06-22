package com.example.meetingtask.domain.repository

import com.example.meetingtask.domain.model.JiraTicketDraft

interface ClientBriefRepository {
    fun generateTicketFromBrief(clientBrief: String): JiraTicketDraft
}
