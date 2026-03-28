package com.willbicks.notificationinspector

import com.willbicks.notificationinspector.model.CapturedNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Thread-safe singleton repository for storing captured notifications.
 * Provides StateFlow for observing changes in the UI.
 */
object NotificationRepository {
  private const val MAX_NOTIFICATIONS = 500

  private val _notificationsFlow = MutableStateFlow<List<CapturedNotification>>(emptyList())
  val notificationsFlow: StateFlow<List<CapturedNotification>> = _notificationsFlow.asStateFlow()

  private val _isListenerConnectedFlow = MutableStateFlow(false)
  val isListenerConnectedFlow: StateFlow<Boolean> = _isListenerConnectedFlow.asStateFlow()

  // Counter for generating unique event IDs
  private var nextEventId: Long = 0L

  /**
   * Add a new captured notification event to the repository.
   */
  @Synchronized
  fun addNotification(notification: CapturedNotification) {
    val notificationWithId = notification.copy(eventId = nextEventId++)

    val currentList = _notificationsFlow.value.toMutableList()

    currentList.add(0, notificationWithId)

    if (currentList.size > MAX_NOTIFICATIONS) {
      currentList.subList(MAX_NOTIFICATIONS, currentList.size).clear()
    }

    _notificationsFlow.value = currentList
  }

  /**
   * Get a notification event by its unique event ID.
   */
  fun getNotificationByEventId(eventId: Long): CapturedNotification? = _notificationsFlow.value.find { it.eventId == eventId }

  /**
   * Clear all captured notification events and reset the ID counter
   */
  @Synchronized
  fun clear() {
    _notificationsFlow.value = emptyList()
    nextEventId = 0L
  }

  /**
   * Get the current count of captured notifications
   */
  fun getCount(): Int = _notificationsFlow.value.size

  /**
   * Update the listener connection state
   */
  fun setListenerConnected(connected: Boolean) {
    _isListenerConnectedFlow.value = connected
  }

  /**
   * Get all notifications (snapshot)
   */
  fun getAllNotifications(): List<CapturedNotification> = _notificationsFlow.value
}
