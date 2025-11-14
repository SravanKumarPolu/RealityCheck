package com.realitycheck.app.data

/**
 * Template for pre-filled decision forms
 */
data class DecisionTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String = "",
    val defaultEnergy24h: Float = 0f,
    val defaultMood24h: Float = 0f,
    val defaultStress24h: Float = 0f,
    val defaultRegretChance24h: Float = 0f,
    val defaultOverallImpact7d: Float = 0f,
    val defaultConfidence: Float = 50f,
    val defaultCheckInDays: Int = 3
) {
    companion object {
        /**
         * Pre-defined templates for common decisions
         */
        val TEMPLATES = listOf(
            DecisionTemplate(
                id = "late_night_screen",
                title = "Late-night screen time",
                category = "Health",
                description = "Watching YouTube/Netflix past midnight",
                defaultEnergy24h = -3f,
                defaultMood24h = -2f,
                defaultStress24h = 1f,
                defaultRegretChance24h = 4f,
                defaultOverallImpact7d = -2f,
                defaultConfidence = 70f,
                defaultCheckInDays = 1
            ),
            DecisionTemplate(
                id = "food_order",
                title = "Order food delivery",
                category = "Money",
                description = "Ordering food instead of cooking",
                defaultEnergy24h = 1f,
                defaultMood24h = 2f,
                defaultStress24h = -1f,
                defaultRegretChance24h = -1f,
                defaultOverallImpact7d = -1f,
                defaultConfidence = 60f,
                defaultCheckInDays = 1
            ),
            DecisionTemplate(
                id = "skip_gym",
                title = "Skip gym workout",
                category = "Health",
                description = "Deciding to skip planned workout",
                defaultEnergy24h = 0f,
                defaultMood24h = -1f,
                defaultStress24h = 1f,
                defaultRegretChance24h = 2f,
                defaultOverallImpact7d = -2f,
                defaultConfidence = 65f,
                defaultCheckInDays = 1
            ),
            DecisionTemplate(
                id = "new_course",
                title = "Buy online course",
                category = "Study",
                description = "Purchasing a new course or learning resource",
                defaultEnergy24h = 2f,
                defaultMood24h = 3f,
                defaultStress24h = -1f,
                defaultRegretChance24h = -2f,
                defaultOverallImpact7d = 3f,
                defaultConfidence = 55f,
                defaultCheckInDays = 7
            ),
            DecisionTemplate(
                id = "new_project",
                title = "Take on new project",
                category = "Work",
                description = "Accepting a new work project or assignment",
                defaultEnergy24h = 1f,
                defaultMood24h = 2f,
                defaultStress24h = 2f,
                defaultRegretChance24h = 0f,
                defaultOverallImpact7d = 2f,
                defaultConfidence = 60f,
                defaultCheckInDays = 3
            ),
            DecisionTemplate(
                id = "impulse_purchase",
                title = "Impulse purchase",
                category = "Money",
                description = "Making an unplanned purchase",
                defaultEnergy24h = 1f,
                defaultMood24h = 2f,
                defaultStress24h = 0f,
                defaultRegretChance24h = 1f,
                defaultOverallImpact7d = -1f,
                defaultConfidence = 50f,
                defaultCheckInDays = 7
            ),
            DecisionTemplate(
                id = "social_event",
                title = "Attend social event",
                category = "Relationships",
                description = "Deciding to attend or skip a social gathering",
                defaultEnergy24h = 2f,
                defaultMood24h = 3f,
                defaultStress24h = -1f,
                defaultRegretChance24h = -1f,
                defaultOverallImpact7d = 1f,
                defaultConfidence = 65f,
                defaultCheckInDays = 7
            ),
            DecisionTemplate(
                id = "stay_up_late",
                title = "Stay up late working",
                category = "Work",
                description = "Working late into the night",
                defaultEnergy24h = -4f,
                defaultMood24h = -2f,
                defaultStress24h = 2f,
                defaultRegretChance24h = 3f,
                defaultOverallImpact7d = -2f,
                defaultConfidence = 70f,
                defaultCheckInDays = 1
            )
        )

        /**
         * Get template by ID
         */
        fun getTemplateById(id: String): DecisionTemplate? {
            return TEMPLATES.find { it.id == id }
        }

        /**
         * Get templates by category
         */
        fun getTemplatesByCategory(category: String): List<DecisionTemplate> {
            return TEMPLATES.filter { it.category == category }
        }
    }
}

