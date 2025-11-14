package com.realitycheck.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.realitycheck.app.data.DecisionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that sends notification when a decision check-in is due
 */
@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: DecisionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val decisionId = inputData.getLong(DECISION_ID_KEY, -1L)
            if (decisionId == -1L) {
                return@withContext Result.failure()
            }

            val decision = repository.getDecisionById(decisionId)
            
            if (decision == null || decision.isCompleted()) {
                // Decision no longer exists or already completed
                return@withContext Result.success()
            }

            // Create notification channel (required for Android 8.0+)
            createNotificationChannel()

            // Build and show notification
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create intent to open the app when notification is clicked
            val intent = android.content.Intent(applicationContext, com.realitycheck.app.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("decision_id", decisionId)
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                applicationContext,
                decisionId.toInt(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(
                applicationContext,
                CHANNEL_ID
            )
                .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Replace with app icon
                .setContentTitle("Time for a Reality Check!")
                .setContentText("Check in on: ${decision.title}")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("It's time to see how your prediction matched reality for: ${decision.title}"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(decisionId.toInt(), notification)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
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
        const val CHANNEL_ID = "reality_check_notifications"
        const val CHANNEL_NAME = "Reality Check Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for decision check-ins"
        const val DECISION_ID_KEY = "decision_id"
    }
}

