package com.example.meetingtask.presentation.brief

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.meetingtask.domain.usecase.ExtractBriefFromPdfUseCase
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase

class BriefGeneratorViewModelFactory(
    private val generateJiraTicketUseCase: GenerateJiraTicketUseCase,
    private val extractBriefFromPdfUseCase: ExtractBriefFromPdfUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(BriefGeneratorViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        val viewModel = BriefGeneratorViewModel(
            generateJiraTicketUseCase = generateJiraTicketUseCase,
            extractBriefFromPdfUseCase = extractBriefFromPdfUseCase
        )
        return modelClass.cast(viewModel)
    }
}
