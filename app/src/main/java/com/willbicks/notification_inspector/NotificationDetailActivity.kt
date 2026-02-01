package com.willbicks.notification_inspector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.willbicks.notification_inspector.model.CapturedNotification
import com.willbicks.notification_inspector.ui.screens.NotificationDetailScreen
import com.willbicks.notification_inspector.ui.theme.NotificationInspectorTheme

/**
 * Activity for displaying detailed information about a captured notification
 */
class NotificationDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get the notification event ID from intent
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) {
            Toast.makeText(this, R.string.notification_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Find the notification event in the repository
        val notification = NotificationRepository.getNotificationByEventId(eventId)
        if (notification == null) {
            Toast.makeText(this, R.string.notification_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            NotificationInspectorTheme {
                NotificationDetailScreen(
                    notification = notification,
                    onNavigateBack = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"

        fun createIntent(context: Context, notification: CapturedNotification): Intent {
            return Intent(context, NotificationDetailActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, notification.eventId)
            }
        }
    }
}
