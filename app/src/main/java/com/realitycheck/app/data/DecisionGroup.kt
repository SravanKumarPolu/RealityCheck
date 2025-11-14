package com.realitycheck.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * DecisionGroup entity for organizing decisions into projects or groups
 */
@Entity(tableName = "decision_groups")
data class DecisionGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: String = "#6C5CE7", // Default primary color
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    companion object {
        /**
         * Default groups for quick start
         */
        val DEFAULT_GROUPS = listOf(
            DecisionGroup(
                id = 1,
                name = "Personal",
                description = "Personal life decisions",
                color = "#6C5CE7"
            ),
            DecisionGroup(
                id = 2,
                name = "Work",
                description = "Work and career decisions",
                color = "#00B894"
            ),
            DecisionGroup(
                id = 3,
                name = "Health",
                description = "Health and fitness decisions",
                color = "#FD79A8"
            )
        )
    }
}

