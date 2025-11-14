package com.realitycheck.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.realitycheck.app.MainActivity
import com.realitycheck.app.data.DecisionTemplate

/**
 * App Widget for quick decision logging
 */
class QuickDecisionWidget : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_QUICK_LOG) {
            val templateId = intent.getStringExtra(EXTRA_TEMPLATE_ID)
            if (templateId != null) {
                // Open app with template pre-selected
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(EXTRA_TEMPLATE_ID, templateId)
                }
                context.startActivity(mainIntent)
            } else {
                // Open app to create decision
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra(EXTRA_QUICK_LOG, true)
                }
                context.startActivity(mainIntent)
            }
        }
    }
    
    companion object {
        private const val ACTION_QUICK_LOG = "com.realitycheck.app.QUICK_LOG"
        const val EXTRA_TEMPLATE_ID = "template_id"
        const val EXTRA_QUICK_LOG = "quick_log"
        
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Create RemoteViews with package name
            val packageName = context.packageName
            val views = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            
            // Quick log button - open app
            val quickLogIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(EXTRA_QUICK_LOG, true)
            }
            val quickLogPendingIntent = PendingIntent.getActivity(
                context,
                0,
                quickLogIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Set text and click action
            views.setTextViewText(android.R.id.text1, "Quick Decision Log")
            views.setOnClickPendingIntent(android.R.id.text1, quickLogPendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

