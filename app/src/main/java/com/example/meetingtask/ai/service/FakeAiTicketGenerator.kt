package com.example.meetingtask.ai.service

import com.example.meetingtask.domain.model.JiraImportTicket
import com.example.meetingtask.domain.model.JiraIssueType

class FakeAiTicketGenerator : AiTicketGenerator {
    override fun generateTickets(rawClientBrief: String): List<JiraImportTicket> {
        return listOf(
            JiraImportTicket(
                summary = "Client Brief Processing Platform",
                issueType = JiraIssueType.EPIC,
                description = "Deliver a flow that converts client brief PDFs into Jira-importable developer tickets.",
                epicName = "Client Brief Processing Platform",
                parentSummary = ""
            ),
            JiraImportTicket(
                summary = "Generate tickets from uploaded brief",
                issueType = JiraIssueType.STORY,
                description = "As a PM, I can upload a brief PDF and generate structured Jira tickets.",
                epicName = "Client Brief Processing Platform",
                parentSummary = ""
            ),
            JiraImportTicket(
                summary = "Implement OpenAI ticket generation service",
                issueType = JiraIssueType.TASK,
                description = "Call OpenAI API and normalize response into strict ticket JSON.",
                epicName = "Client Brief Processing Platform",
                parentSummary = "Generate tickets from uploaded brief"
            )
        )
    }
}
