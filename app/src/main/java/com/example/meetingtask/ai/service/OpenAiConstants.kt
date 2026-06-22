package com.example.meetingtask.ai.service

object OpenAiConstants {
    const val CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions"
    const val DEFAULT_MODEL = "gpt-4.1-mini"
    const val HTTP_METHOD_POST = "POST"
    const val CONNECT_TIMEOUT_MS = 30_000
    const val READ_TIMEOUT_MS = 60_000
    const val HEADER_AUTHORIZATION = "Authorization"
    const val AUTHORIZATION_BEARER_PREFIX = "Bearer "
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE_JSON = "application/json"
    const val ROLE_SYSTEM = "system"
    const val ROLE_USER = "user"
    const val SYSTEM_PROMPT =
        "Return only JSON for Jira CSV import. Include tickets array with strict fields: summary, issue_type, description, epic_name, parent_summary."
    const val HTTP_SUCCESS_MIN = 200
    const val HTTP_SUCCESS_MAX = 299
}
