package com.example.meetingtask.presentation.brief

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meetingtask.domain.model.JiraImportTicket
import com.example.meetingtask.domain.usecase.ExtractBriefFromPdfUseCase
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BriefGeneratorViewModel(
    private val generateJiraTicketUseCase: GenerateJiraTicketUseCase,
    private val extractBriefFromPdfUseCase: ExtractBriefFromPdfUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BriefGeneratorUiState())
    val uiState: StateFlow<BriefGeneratorUiState> = _uiState.asStateFlow()

    fun loadBriefFromPdf(inputStream: InputStream) {
        updateUiState {
            copy(
                isPdfLoading = true,
                pdfStatusMessage = "Reading PDF...",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    extractBriefFromPdfUseCase(inputStream)
                }
            }.onSuccess { text ->
                updateUiState {
                    copy(
                        isPdfLoading = false,
                        extractedBriefText = text,
                        pdfStatusMessage = if (text.isBlank()) {
                            "Selected PDF has no readable text"
                        } else {
                            "PDF loaded (${text.length} chars)"
                        }
                    )
                }
            }.onFailure { throwable ->
                updateUiState {
                    copy(
                        isPdfLoading = false,
                        extractedBriefText = "",
                        pdfStatusMessage = "Failed to read PDF",
                        errorMessage = throwable.message ?: "PDF parsing failed"
                    )
                }
            }
        }
    }

    fun onPdfOpenFailed(message: String) {
        updateUiState {
            copy(
                isPdfLoading = false,
                extractedBriefText = "",
                pdfStatusMessage = "Failed to read PDF",
                errorMessage = message
            )
        }
    }

    fun onGenerateClicked() {
        val clientBrief = _uiState.value.extractedBriefText
        if (clientBrief.isBlank()) {
            updateUiState {
                copy(
                    errorMessage = "Client brief cannot be empty.",
                    ticketCount = 0,
                    csvPreview = "",
                    csvContent = "",
                    reportContent = ""
                )
            }
            return
        }

        updateUiState { copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    generateJiraTicketUseCase(clientBrief)
                }
            }.onSuccess { draft ->
                val preview = draft.csvContent
                    .lineSequence()
                    .take(12)
                    .joinToString("\n")
                val report = buildHumanReadableReport(draft.tickets)
                updateUiState {
                    copy(
                        isLoading = false,
                        ticketCount = draft.tickets.size,
                        csvPreview = preview,
                        csvContent = draft.csvContent,
                        reportContent = report,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                updateUiState {
                    copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unknown error",
                        ticketCount = 0,
                        csvPreview = "",
                        csvContent = "",
                        reportContent = ""
                    )
                }
            }
        }
    }

    private fun updateUiState(block: BriefGeneratorUiState.() -> BriefGeneratorUiState) {
        _uiState.update { it.block() }
    }

    private fun buildHumanReadableReport(tickets: List<JiraImportTicket>): String {
        if (tickets.isEmpty()) return "No tickets generated."
        return buildString {
            appendLine("Jira Developer Tickets Report")
            appendLine()
            tickets.forEachIndexed { index, ticket ->
                appendLine("${index + 1}. [${ticket.issueType.label}] ${ticket.summary}")
                appendLine("   Epic: ${ticket.epicName.ifBlank { "-" }}")
                appendLine("   Parent: ${ticket.parentSummary.ifBlank { "-" }}")
                appendLine("   Description: ${ticket.description}")
                appendLine()
            }
        }.trim()
    }
}
