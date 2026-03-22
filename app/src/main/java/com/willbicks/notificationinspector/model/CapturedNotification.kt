package com.willbicks.notificationinspector.model

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
  val postTime: Long,
  // The field tree for the detail view
  val fields: Map<NotificationSection, List<NotificationField>> = emptyMap(),
) {
  enum class EventType {
    POSTED,
    REMOVED,
  }

  companion object {
    private val CAPTURE_TIME_FORMAT = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val POST_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
  }

  /**
   * Returns a formatted timestamp string for display
   */
  fun getFormattedCaptureTime(): String = CAPTURE_TIME_FORMAT.format(Date(captureTime))

  fun getFormattedPostTime(): String = POST_TIME_FORMAT.format(Date(postTime))

  /**
   * Returns a brief summary for list display
   */
  fun getSummary(): String = title ?: text ?: packageName
}
