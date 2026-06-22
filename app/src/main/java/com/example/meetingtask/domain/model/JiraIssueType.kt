package com.example.meetingtask.domain.model

enum class JiraIssueType(val label: String) {
    EPIC("Epic"),
    STORY("Story"),
    TASK("Task"),
    UNKNOWN("Task");

    companion object {
        fun fromRaw(value: String?): JiraIssueType {
            return when (value?.trim().orEmpty().lowercase()) {
                "epic" -> EPIC
                "story", "user story", "user_story" -> STORY
                "task" -> TASK
                else -> UNKNOWN
            }
        }
    }
}
