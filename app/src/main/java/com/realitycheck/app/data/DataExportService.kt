package com.realitycheck.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

import javax.inject.Inject

/**
 * Service for exporting decision data to various formats
 */
class DataExportService @Inject constructor(
    private val repository: DecisionRepository
) {

    /**
     * Export all decisions to CSV format
     */
    suspend fun exportToCsv(context: Context): File = withContext(Dispatchers.IO) {
        val decisionsFlow = repository.getAllDecisions()
        val decisionsList = decisionsFlow.first()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "realitycheck_export_${dateFormat.format(Date())}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileWriter(file).use { writer ->
            // Write CSV header
            writer.append("ID,Title,Category,Tags,Created Date,Reminder Date,")
            writer.append("Predicted Energy,Predicted Mood,Predicted Stress,Predicted Regret,Overall Impact,Confidence,")
            writer.append("Actual Energy,Actual Mood,Actual Stress,Actual Regret,")
            writer.append("Followed Decision,Outcome,Outcome Date,Accuracy,Regret Index\n")
            
            // Write data rows
            decisionsList.forEach { decision: Decision ->
                writer.append("${decision.id},")
                writer.append("\"${escapeCsv(decision.title)}\",")
                writer.append("${decision.category ?: "Other"},")
                writer.append("\"${decision.tags.joinToString("; ")}\",")
                writer.append("${formatDate(decision.createdAt)},")
                writer.append("${decision.reminderDate?.let { formatDate(it) } ?: ""},")
                
                // Predictions
                writer.append("${decision.predictedEnergy24h ?: ""},")
                writer.append("${decision.predictedMood24h ?: ""},")
                writer.append("${decision.predictedStress24h ?: ""},")
                writer.append("${decision.predictedRegretChance24h ?: ""},")
                writer.append("${decision.predictedOverallImpact7d ?: ""},")
                writer.append("${decision.predictionConfidence ?: ""},")
                
                // Outcomes
                writer.append("${decision.actualEnergy24h ?: ""},")
                writer.append("${decision.actualMood24h ?: ""},")
                writer.append("${decision.actualStress24h ?: ""},")
                writer.append("${decision.actualRegret24h ?: ""},")
                writer.append("${decision.followedDecision ?: ""},")
                writer.append("\"${escapeCsv(decision.outcome ?: "")}\",")
                writer.append("${decision.outcomeRecordedAt?.let { formatDate(it) } ?: ""},")
                writer.append("${decision.getAccuracy() ?: ""},")
                writer.append("${decision.getRegretIndex() ?: ""}\n")
            }
        }
        
        file
    }

    /**
     * Export all decisions to JSON format
     */
    suspend fun exportToJson(context: Context): File = withContext(Dispatchers.IO) {
        val decisionsFlow = repository.getAllDecisions()
        val decisionsList = decisionsFlow.first()
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val fileName = "realitycheck_export_${dateFormat.format(Date())}.json"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        val jsonArray = JSONArray()
        
        decisionsList.forEach { decision: Decision ->
            val jsonObject = JSONObject().apply {
                put("id", decision.id)
                put("title", decision.title)
                put("category", decision.category ?: "Other")
                put("tags", JSONArray(decision.tags))
                put("createdAt", decision.createdAt.time)
                put("reminderDate", decision.reminderDate?.time ?: JSONObject.NULL)
                
                // Predictions
                put("predictedEnergy24h", decision.predictedEnergy24h ?: JSONObject.NULL)
                put("predictedMood24h", decision.predictedMood24h ?: JSONObject.NULL)
                put("predictedStress24h", decision.predictedStress24h ?: JSONObject.NULL)
                put("predictedRegretChance24h", decision.predictedRegretChance24h ?: JSONObject.NULL)
                put("predictedOverallImpact7d", decision.predictedOverallImpact7d ?: JSONObject.NULL)
                put("predictionConfidence", decision.predictionConfidence ?: JSONObject.NULL)
                
                // Outcomes
                put("actualEnergy24h", decision.actualEnergy24h ?: JSONObject.NULL)
                put("actualMood24h", decision.actualMood24h ?: JSONObject.NULL)
                put("actualStress24h", decision.actualStress24h ?: JSONObject.NULL)
                put("actualRegret24h", decision.actualRegret24h ?: JSONObject.NULL)
                put("followedDecision", decision.followedDecision ?: JSONObject.NULL)
                put("outcome", decision.outcome ?: JSONObject.NULL)
                put("outcomeRecordedAt", decision.outcomeRecordedAt?.time ?: JSONObject.NULL)
                put("accuracy", decision.getAccuracy() ?: JSONObject.NULL)
                put("regretIndex", decision.getRegretIndex() ?: JSONObject.NULL)
            }
            jsonArray.put(jsonObject)
        }
        
        val exportData = JSONObject().apply {
            put("exportDate", Date().time)
            put("version", "1.0")
            put("totalDecisions", decisionsList.size)
            put("decisions", jsonArray)
        }
        
        FileWriter(file).use { writer ->
            writer.write(exportData.toString(2)) // Pretty print with 2-space indent
        }
        
        file
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"") // Escape quotes
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(date)
    }
}

