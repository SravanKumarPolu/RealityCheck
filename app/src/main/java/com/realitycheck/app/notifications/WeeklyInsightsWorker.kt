package com.realitycheck.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import com.realitycheck.app.data.DecisionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker that sends weekly insights notification
 */
@HiltWorker
class WeeklyInsightsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: DecisionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val decisionsFlow = repository.getAllDecisions()
            val decisions = decisionsFlow.first()
            
            val completed = decisions.filter { it.isCompleted() }
            if (completed.isEmpty()) {
                return@withContext Result.success() // No data to show
            }
            
            // Calculate insights
            val avgAccuracy = completed.mapNotNull { it.getAccuracy() }.average().toFloat()
            val streak = calculateStreak(decisions)
            val topRegretCategory = getTopRegretCategory(completed)
            
            // Create notification channel
            createNotificationChannel()
            
            // Build notification
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            val message = buildWeeklyMessage(avgAccuracy, streak, topRegretCategory)
            
            val notification = NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("📊 Your Weekly Decision Insights")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(WEEKLY_INSIGHTS_NOTIFICATION_ID, notification)
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun buildWeeklyMessage(
        avgAccuracy: Float,
        streak: Int,
        topRegretCategory: String?
    ): String {
        val accuracyMsg = "Your accuracy: ${avgAccuracy.toInt()}%"
        val streakMsg = if (streak > 0) "🔥 $streak day streak" else ""
        val regretMsg = topRegretCategory?.let { "Watch out for: $it" } ?: ""
        
        return listOf(accuracyMsg, streakMsg, regretMsg)
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
    
    private fun calculateStreak(decisions: List<com.realitycheck.app.data.Decision>): Int {
        if (decisions.isEmpty()) return 0
        
        val decisionDays = decisions.map { decision ->
            val calendar = java.util.Calendar.getInstance().apply {
                time = decision.createdAt
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            calendar.time
        }.distinct().sortedDescending()
        
        if (decisionDays.isEmpty()) return 0
        
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        var streak = 0
        val calendar = java.util.Calendar.getInstance()
        calendar.time = today
        
        while (true) {
            val dayToCheck = calendar.time
            val hasDecision = decisionDays.any { decisionDay ->
                val daysDiff = ((dayToCheck.time - decisionDay.time) / (1000 * 60 * 60 * 24)).toInt()
                daysDiff == 0
            }
            
            if (hasDecision) {
                streak++
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun getTopRegretCategory(completed: List<com.realitycheck.app.data.Decision>): String? {
        val regretByCategory = completed
            .filter { it.getRegretIndex() != null && it.category != null }
            .groupBy { it.category!! }
            .mapValues { (_, decisions) ->
                decisions.mapNotNull { it.getRegretIndex() }.average().toFloat()
            }
        
        return regretByCategory.maxByOrNull { it.value }?.key
    }
    
    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        const val CHANNEL_ID = "weekly_insights_notifications"
        const val CHANNEL_NAME = "Weekly Insights"
        const val CHANNEL_DESCRIPTION = "Weekly decision insights and summaries"
        const val WEEKLY_INSIGHTS_NOTIFICATION_ID = 9999
        
        /**
         * Schedule weekly insights notification
         */
        fun scheduleWeeklyInsights(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WeeklyInsightsWorker>(
                7, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag("weekly_insights")
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "weekly_insights",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

