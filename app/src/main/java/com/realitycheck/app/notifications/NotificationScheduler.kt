package com.realitycheck.app.notifications

import android.content.Context
import androidx.work.*
import com.realitycheck.app.data.Decision
import java.util.concurrent.TimeUnit

/**
 * Utility class for scheduling notification reminders for decisions
 */
object NotificationScheduler {

    /**
     * Schedule a notification for a decision's check-in reminder
     */
    fun scheduleNotification(context: Context, decision: Decision) {
        if (decision.reminderDate == null || decision.isCompleted()) {
            return
        }

        val now = System.currentTimeMillis()
        val reminderTime = decision.reminderDate.time
        val delay = reminderTime - now

        // Only schedule if reminder is in the future
        if (delay <= 0) {
            return
        }

        val inputData = Data.Builder()
            .putLong(NotificationWorker.DECISION_ID_KEY, decision.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("decision_${decision.id}")
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * Cancel a scheduled notification for a decision
     */
    fun cancelNotification(context: Context, decisionId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("decision_$decisionId")
    }

    /**
     * Cancel all notifications (useful for testing or reset)
     */
    fun cancelAllNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }
}

