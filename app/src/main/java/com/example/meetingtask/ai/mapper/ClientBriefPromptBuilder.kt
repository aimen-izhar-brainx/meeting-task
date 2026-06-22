package com.example.meetingtask.ai.mapper

class ClientBriefPromptBuilder {
    fun build(clientBrief: String): String {
        return """
            Convert this client brief into a Jira ticket.
            Return a concise title and clear technical description.

            Client brief:
            $clientBrief
        """.trimIndent()
    }
}
