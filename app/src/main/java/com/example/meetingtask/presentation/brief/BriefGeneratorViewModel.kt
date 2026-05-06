package com.example.meetingtask.presentation.brief

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase

class BriefGeneratorViewModel(
    private val generateJiraTicketUseCase: GenerateJiraTicketUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(BriefGeneratorUiState())
    val uiState: LiveData<BriefGeneratorUiState> = _uiState

    fun onGenerateClicked(clientBrief: String) {
        if (clientBrief.isBlank()) {
            _uiState.value = _uiState.value?.copy(
                errorMessage = "Client brief cannot be empty.",
                ticketCount = 0,
                csvPreview = "",
                csvContent = "",
                reportContent = ""
            )
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)

        Thread {
            runCatching {
                generateJiraTicketUseCase(clientBrief)
            }.onSuccess { draft ->
                val preview = draft.csvContent
                    .lineSequence()
                    .take(12)
                    .joinToString("\n")
                val report = buildHumanReadableReport(draft.tickets)
                _uiState.postValue(
                    _uiState.value?.copy(
                        isLoading = false,
                        ticketCount = draft.tickets.size,
                        csvPreview = preview,
                        csvContent = draft.csvContent,
                        reportContent = report,
                        errorMessage = null
                    )
                )
            }.onFailure { throwable ->
                _uiState.postValue(
                    _uiState.value?.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unknown error",
                        ticketCount = 0,
                        csvPreview = "",
                        csvContent = "",
                        reportContent = ""
                    )
                )
            }
        }.start()
    }

    private fun buildHumanReadableReport(tickets: List<com.example.meetingtask.domain.model.JiraImportTicket>): String {
        if (tickets.isEmpty()) return "No tickets generated."
        return buildString {
            appendLine("Jira Developer Tickets Report")
            appendLine()
            tickets.forEachIndexed { index, ticket ->
                appendLine("${index + 1}. [${ticket.issueType}] ${ticket.summary}")
                appendLine("   Epic: ${ticket.epicName.ifBlank { "-" }}")
                appendLine("   Parent: ${ticket.parentSummary.ifBlank { "-" }}")
                appendLine("   Description: ${ticket.description}")
                appendLine()
            }
        }.trim()
    }
}
