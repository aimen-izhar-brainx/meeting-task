package com.example.meetingtask.ai.mapper

class PromptBuilder {
    fun buildPrompt(input: String): String {
        return """
            Convert this client brief into developer Jira tickets for CSV import.
            Do not assume missing details.
            If required information is missing, set field value to "NEEDS CLARIFICATION".
            Return ONLY valid JSON in this exact structure:
            {
              "tickets": [
                {
                  "summary": "",
                  "issue_type": "Epic|Story|Task",
                  "description": "",
                  "epic_name": "",
                  "parent_summary": ""
                }
              ]
            }

            Rules:
            - No markdown, no explanation text, JSON only.
            - Use issue_type values exactly: Epic, Story, Task.
            - For Epic rows: epic_name should match summary, parent_summary should be empty.
            - For Story rows: epic_name should reference the epic summary, parent_summary should be empty.
            - For Task rows: epic_name should reference the epic summary, parent_summary should reference a Story summary.
            - Ensure all fields are present for every ticket object.

            Client brief:
            $input
        """.trimIndent()
    }
}
