package com.example.meetingtask.data.source.local

import com.example.meetingtask.domain.repository.BriefPdfReader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

class PdfTextExtractor : BriefPdfReader {

    override fun extractText(inputStream: InputStream): String {
        PDDocument.load(inputStream).use { document ->
            return PDFTextStripper().getText(document).trim()
        }
    }
}
