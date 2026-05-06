package com.example.meetingtask.domain.model

data class JiraImportTicket(
    val summary: String,
    val issueType: String,
    val description: String,
    val epicName: String,
    val parentSummary: String
)
