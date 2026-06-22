package com.example.meetingtask.presentation.brief

import android.content.Context
import com.example.meetingtask.BuildConfig
import com.example.meetingtask.ai.service.AiTicketGenerator
import com.example.meetingtask.ai.service.FakeAiTicketGenerator
import com.example.meetingtask.ai.service.OpenAiTicketGenerator
import com.example.meetingtask.data.repository.ClientBriefRepositoryImpl
import com.example.meetingtask.data.source.local.PdfTextExtractor
import com.example.meetingtask.domain.usecase.ExtractBriefFromPdfUseCase
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

object BriefGeneratorDependencies {

    fun createViewModelFactory(context: Context): BriefGeneratorViewModelFactory {
        PDFBoxResourceLoader.init(context.applicationContext)

        val aiGenerator: AiTicketGenerator = if (BuildConfig.OPENAI_API_KEY.isBlank()) {
            FakeAiTicketGenerator()
        } else {
            OpenAiTicketGenerator(BuildConfig.OPENAI_API_KEY)
        }
        val repository = ClientBriefRepositoryImpl(aiGenerator)
        val generateJiraTicketUseCase = GenerateJiraTicketUseCase(repository)
        val extractBriefFromPdfUseCase = ExtractBriefFromPdfUseCase(PdfTextExtractor())

        return BriefGeneratorViewModelFactory(
            generateJiraTicketUseCase = generateJiraTicketUseCase,
            extractBriefFromPdfUseCase = extractBriefFromPdfUseCase
        )
    }
}
