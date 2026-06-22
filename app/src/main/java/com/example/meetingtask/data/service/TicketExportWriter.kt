package com.example.meetingtask.data.service

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import java.io.OutputStreamWriter

class TicketExportWriter {

    fun writeCsv(outputStream: OutputStream, csvContent: String) {
        require(csvContent.isNotBlank()) { "No CSV content to export." }
        OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
            writer.write(csvContent)
            writer.flush()
        }
    }

    fun writePdfReport(outputStream: OutputStream, reportContent: String) {
        require(reportContent.isNotBlank()) { "No report content to export." }

        val pageWidth = 595
        val pageHeight = 842
        val margin = 40
        val lineHeight = 18
        val paint = Paint().apply { textSize = 12f }
        val titlePaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
        }

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
        pdf.writeTo(outputStream)
        pdf.close()
    }
}
