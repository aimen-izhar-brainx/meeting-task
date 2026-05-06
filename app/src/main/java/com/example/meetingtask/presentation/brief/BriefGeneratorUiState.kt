package com.example.meetingtask.presentation.brief

data class BriefGeneratorUiState(
    val isLoading: Boolean = false,
    val ticketCount: Int = 0,
    val csvPreview: String = "",
    val csvContent: String = "",
    val reportContent: String = "",
    val errorMessage: String? = null
)
