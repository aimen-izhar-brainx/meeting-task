package com.example.meetingtask.ai.service

interface AiTicketGenerator {
    fun generateTicketJson(rawClientBrief: String): String
}
