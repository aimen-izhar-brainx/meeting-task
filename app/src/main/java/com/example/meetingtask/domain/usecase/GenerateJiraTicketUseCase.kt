package com.example.meetingtask.domain.usecase

import com.example.meetingtask.domain.model.JiraTicketDraft
import com.example.meetingtask.domain.repository.ClientBriefRepository

class GenerateJiraTicketUseCase(
    private val repository: ClientBriefRepository
) {
    operator fun invoke(clientBrief: String): JiraTicketDraft {
        return repository.generateTicketFromBrief(clientBrief)
    }
}
