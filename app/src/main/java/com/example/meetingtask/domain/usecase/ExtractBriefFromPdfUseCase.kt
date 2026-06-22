package com.example.meetingtask.domain.usecase

import com.example.meetingtask.domain.repository.BriefPdfReader
import java.io.InputStream

class ExtractBriefFromPdfUseCase(
    private val briefPdfReader: BriefPdfReader
) {
    operator fun invoke(inputStream: InputStream): String {
        return briefPdfReader.extractText(inputStream)
    }
}
