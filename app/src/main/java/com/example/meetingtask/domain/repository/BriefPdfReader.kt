package com.example.meetingtask.domain.repository

import java.io.InputStream

interface BriefPdfReader {
    fun extractText(inputStream: InputStream): String
}
