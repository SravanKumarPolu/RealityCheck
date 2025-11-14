package com.realitycheck.app.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat

/**
 * Helper class for managing notification permissions
 * Handles permission checks and requests for Android 13+ (API 33+)
 */
object NotificationPermissionHelper {
    
    /**
     * Check if notification permission is granted
     * For Android 12 and below, notifications are enabled by default
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires explicit permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 12 and below: notifications are enabled by default
            // Check if notifications are enabled in system settings
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            notificationManager.areNotificationsEnabled()
        }
    }
    
    /**
     * Request notification permission
     * Should be called from an Activity context
     */
    fun requestNotificationPermission(activity: ComponentActivity, requestCode: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                requestCode
            )
        }
        // For Android 12 and below, no permission request needed
    }
    
    /**
     * Check if exact alarm permission is needed
     * Required for Android 12+ (API 31+) for precise scheduling
     */
    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Android 11 and below don't require this permission
        }
    }
    
    /**
     * Open system settings to enable exact alarm scheduling
     * For Android 12+, user must enable this manually
     */
    fun openExactAlarmSettings(activity: ComponentActivity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            )
            activity.startActivity(intent)
        }
    }
    
    /**
     * Check if notifications can be shown
     * Returns true if both notification permission and channel are enabled
     */
    fun canShowNotifications(context: Context): Boolean {
        if (!hasNotificationPermission(context)) {
            return false
        }
        
        // Check if notification channel is enabled (Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            val channel = notificationManager.getNotificationChannel(
                NotificationWorker.CHANNEL_ID
            )
            // Channel might be null if not created yet, which is fine
            return channel?.importance != android.app.NotificationManager.IMPORTANCE_NONE
        }
        
        return true
    }
}

