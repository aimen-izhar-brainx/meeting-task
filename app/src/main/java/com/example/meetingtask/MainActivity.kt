package com.example.meetingtask

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.meetingtask.ai.service.AiTicketGenerator
import com.example.meetingtask.ai.service.FakeAiTicketGenerator
import com.example.meetingtask.ai.service.OpenAiTicketGenerator
import com.example.meetingtask.data.repository.ClientBriefRepositoryImpl
import com.example.meetingtask.databinding.ActivityMainBinding
import com.example.meetingtask.domain.usecase.GenerateJiraTicketUseCase
import com.example.meetingtask.presentation.brief.BriefGeneratorViewModel
import com.example.meetingtask.presentation.brief.BriefGeneratorViewModelFactory
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.OutputStreamWriter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var extractedBriefText: String = ""
    private var latestCsvContent: String = ""
    private var latestReportContent: String = ""

    private val openPdfLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        readPdfContent(uri)
    }

    private val exportCsvLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@registerForActivityResult
        saveCsv(uri, latestCsvContent)
    }

    private val exportPdfLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri == null) return@registerForActivityResult
        savePdfReport(uri, latestReportContent)
    }

    private val viewModel: BriefGeneratorViewModel by viewModels {
        val aiGenerator: AiTicketGenerator = if (BuildConfig.OPENAI_API_KEY.isBlank()) {
            FakeAiTicketGenerator()
        } else {
            OpenAiTicketGenerator(BuildConfig.OPENAI_API_KEY)
        }
        val repository = ClientBriefRepositoryImpl(aiGenerator)
        val useCase = GenerateJiraTicketUseCase(repository)
        BriefGeneratorViewModelFactory(useCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectPdfButton.setOnClickListener {
            openPdfLauncher.launch(arrayOf("application/pdf"))
        }

        binding.generateButton.setOnClickListener {
            viewModel.onGenerateClicked(extractedBriefText)
        }

        binding.exportCsvButton.setOnClickListener {
            exportCsvLauncher.launch("jira-import-tickets.csv")
        }

        binding.exportPdfButton.setOnClickListener {
            exportPdfLauncher.launch("jira-human-readable-report.pdf")
        }

        viewModel.uiState.observe(this) { state ->
            binding.generateButton.isEnabled = !state.isLoading
            binding.loadingProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.errorText.text = state.errorMessage.orEmpty()
            binding.ticketCountText.text = "Generated tickets: ${state.ticketCount}"
            binding.csvPreviewText.text = if (state.csvPreview.isBlank()) "-" else state.csvPreview
            latestCsvContent = state.csvContent
            latestReportContent = state.reportContent
            binding.exportCsvButton.isEnabled = state.csvContent.isNotBlank() && !state.isLoading
            binding.exportPdfButton.isEnabled = state.reportContent.isNotBlank() && !state.isLoading
        }
    }

    private fun readPdfContent(uri: Uri) {
        binding.selectedPdfText.text = "Reading PDF..."

        Thread {
            runCatching {
                contentResolver.openInputStream(uri).use { inputStream ->
                    requireNotNull(inputStream) { "Unable to open selected PDF." }
                    PDDocument.load(inputStream).use { document ->
                        PDFTextStripper().getText(document).trim()
                    }
                }
            }.onSuccess { text ->
                extractedBriefText = text
                runOnUiThread {
                    binding.selectedPdfText.text = if (text.isBlank()) {
                        "Selected PDF has no readable text"
                    } else {
                        "PDF loaded (${text.length} chars)"
                    }
                }
            }.onFailure { throwable ->
                extractedBriefText = ""
                runOnUiThread {
                    binding.selectedPdfText.text = "Failed to read PDF"
                    Toast.makeText(
                        this,
                        throwable.message ?: "PDF parsing failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }

    private fun saveCsv(uri: Uri, csvContent: String) {
        if (csvContent.isBlank()) {
            Toast.makeText(this, "No CSV content to export", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            contentResolver.openOutputStream(uri).use { stream ->
                requireNotNull(stream) { "Unable to write CSV file." }
                OutputStreamWriter(stream, Charsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                    writer.flush()
                }
            }
        }.onSuccess {
            Toast.makeText(this, "CSV exported successfully", Toast.LENGTH_SHORT).show()
        }.onFailure { throwable ->
            Toast.makeText(
                this,
                throwable.message ?: "Failed to export CSV",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun savePdfReport(uri: Uri, reportContent: String) {
        if (reportContent.isBlank()) {
            Toast.makeText(this, "No report content to export", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 40
            val lineHeight = 18
            val paint = Paint().apply { textSize = 12f }
            val titlePaint = Paint().apply { textSize = 16f; isFakeBoldText = true }

            val pdf = PdfDocument()
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdf.startPage(pageInfo)
            var canvas = page.canvas
            var y = margin

            canvas.drawText("Jira Human-Readable Report", margin.toFloat(), y.toFloat(), titlePaint)
            y += lineHeight * 2

            reportContent.lineSequence().forEach { line ->
                if (y > pageHeight - margin) {
                    pdf.finishPage(page)
                    pageNumber += 1
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdf.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin
                }
                canvas.drawText(line.take(110), margin.toFloat(), y.toFloat(), paint)
                y += lineHeight
            }
            pdf.finishPage(page)

            contentResolver.openOutputStream(uri).use { stream ->
                requireNotNull(stream) { "Unable to write PDF file." }
                pdf.writeTo(stream)
            }
            pdf.close()
        }.onSuccess {
            Toast.makeText(this, "PDF report exported successfully", Toast.LENGTH_SHORT).show()
        }.onFailure { throwable ->
            Toast.makeText(
                this,
                throwable.message ?: "Failed to export PDF report",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
