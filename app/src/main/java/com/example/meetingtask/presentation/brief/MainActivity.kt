package com.example.meetingtask.presentation.brief

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.example.meetingtask.R
import com.example.meetingtask.data.service.TicketExportWriter
import com.example.meetingtask.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ticketExportWriter = TicketExportWriter()

    private val openPdfLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@registerForActivityResult
        onPdfSelected(uri)
    }

    private val exportCsvLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@registerForActivityResult
        exportCsv(uri)
    }

    private val exportPdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        exportPdfReport(uri)
    }

    private val viewModel: BriefGeneratorViewModel by viewModels {
        BriefGeneratorDependencies.createViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectPdfButton.setOnClickListener {
            openPdfLauncher.launch(arrayOf("application/pdf"))
        }

        binding.generateButton.setOnClickListener {
            viewModel.onGenerateClicked()
        }

        binding.exportCsvButton.setOnClickListener {
            exportCsvLauncher.launch("jira-import-tickets.csv")
        }

        binding.exportPdfButton.setOnClickListener {
            exportPdfLauncher.launch("jira-human-readable-report.pdf")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.generateButton.isEnabled = !state.isLoading && !state.isPdfLoading
                    binding.loadingProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.selectedPdfText.text = state.pdfStatusMessage
                    binding.errorText.text = state.errorMessage.orEmpty()
                    binding.ticketCountText.text = getString(
                        R.string.generated_tickets_count,
                        state.ticketCount
                    )
                    binding.csvPreviewText.text = if (state.csvPreview.isBlank()) {
                        getString(R.string.csv_preview_placeholder)
                    } else {
                        state.csvPreview
                    }
                    binding.exportCsvButton.isEnabled = state.csvContent.isNotBlank() && !state.isLoading
                    binding.exportPdfButton.isEnabled = state.reportContent.isNotBlank() && !state.isLoading
                }
            }
        }
    }

    private fun onPdfSelected(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            viewModel.onPdfOpenFailed("Unable to open selected PDF.")
            return
        }
        inputStream.use { stream ->
            viewModel.loadBriefFromPdf(stream)
        }
    }

    private fun exportCsv(uri: Uri) {
        val csvContent = viewModel.uiState.value.csvContent
        if (csvContent.isBlank()) {
            Toast.makeText(this, "No CSV content to export", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            val outputStream = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Unable to write CSV file.")
            outputStream.use { stream ->
                ticketExportWriter.writeCsv(stream, csvContent)
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

    private fun exportPdfReport(uri: Uri) {
        val reportContent = viewModel.uiState.value.reportContent
        if (reportContent.isBlank()) {
            Toast.makeText(this, "No report content to export", Toast.LENGTH_SHORT).show()
            return
        }

        runCatching {
            val outputStream = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Unable to write PDF file.")
            outputStream.use { stream ->
                ticketExportWriter.writePdfReport(stream, reportContent)
            }
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
