package com.example.meetingtask.presentation.brief

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase

class BriefGeneratorViewModelFactory(
    private val generateJiraTicketUseCase: GenerateJiraTicketUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BriefGeneratorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BriefGeneratorViewModel(generateJiraTicketUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
