package com.example.meetingtask.ai.service

import org.json.JSONArray
import org.json.JSONObject

class FakeAiTicketGenerator : AiTicketGenerator {
    override fun generateTicketJson(rawClientBrief: String): String {
        val tickets = JSONArray()
            .put(
                JSONObject()
                    .put("summary", "Client Brief Processing Platform")
                    .put("issue_type", "Epic")
                    .put("description", "Deliver a flow that converts client brief PDFs into Jira-importable developer tickets.")
                    .put("epic_name", "Client Brief Processing Platform")
                    .put("parent_summary", "")
            )
            .put(
                JSONObject()
                    .put("summary", "Generate tickets from uploaded brief")
                    .put("issue_type", "Story")
                    .put("description", "As a PM, I can upload a brief PDF and generate structured Jira tickets.")
                    .put("epic_name", "Client Brief Processing Platform")
                    .put("parent_summary", "")
            )
            .put(
                JSONObject()
                    .put("summary", "Implement OpenAI ticket generation service")
                    .put("issue_type", "Task")
                    .put("description", "Call OpenAI API and normalize response into strict ticket JSON.")
                    .put("epic_name", "Client Brief Processing Platform")
                    .put("parent_summary", "Generate tickets from uploaded brief")
            )

        return JSONObject().put("tickets", tickets).toString()
    }
}
