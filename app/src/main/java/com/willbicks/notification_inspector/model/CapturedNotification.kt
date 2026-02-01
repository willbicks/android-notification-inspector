package com.willbicks.notification_inspector.model

import android.app.Notification
import android.service.notification.StatusBarNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a captured notification event.
 */
data class CapturedNotification(
    val eventId: Long = 0L,
    
    // Event metadata
    val captureTime: Long = System.currentTimeMillis(),
    val eventType: EventType,
    
    val key: String,
    val packageName: String,
    
    // Content
    val title: String?,
    val text: String?,
    val postTime: Long
) {
    enum class EventType {
        POSTED,
        REMOVED
    }

    companion object {
        private val CAPTURE_TIME_FORMAT = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        private val POST_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }

    /**
     * Returns a formatted timestamp string for display
     */
    fun getFormattedCaptureTime(): String {
        return CAPTURE_TIME_FORMAT.format(Date(captureTime))
    }

    fun getFormattedPostTime(): String {
        return POST_TIME_FORMAT.format(Date(postTime))
    }
    
    /**
     * Returns a brief summary for list display
     */
    fun getSummary(): String {
        return title ?: text ?: packageName
    }
    
    /**
     * Returns full debug output as a formatted string
     */
    fun toDebugString(): String {
        return buildString {
            appendLine("=== Captured Notification ===")
            appendLine()
            appendLine("Event Type: $eventType")
            appendLine("Capture Time: ${getFormattedCaptureTime()}")
            appendLine("Post Time: ${getFormattedPostTime()}")
            appendLine()
            appendLine("Package: $packageName")
            appendLine("Key: $key")
            appendLine()
            appendLine("Title: ${title ?: "(none)"}")
            appendLine("Text: ${text ?: "(none)"}")
        }
    }
    
    companion object {
        /**
         * Factory method to create a CapturedNotification from a StatusBarNotification
         */
        fun fromStatusBarNotification(
            sbn: StatusBarNotification,
            eventType: EventType
        ): CapturedNotification {
            val notification = sbn.notification
            val extras = notification.extras
            
            return CapturedNotification(
                eventType = eventType,
                key = sbn.key,
                packageName = sbn.packageName,
                postTime = sbn.postTime,
                title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            )
        }
    }
}
