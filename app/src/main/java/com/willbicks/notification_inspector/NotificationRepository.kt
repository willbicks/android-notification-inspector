package com.willbicks.notification_inspector

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.willbicks.notification_inspector.model.CapturedNotification

/**
 * Thread-safe singleton repository for storing captured notifications.
 * Provides LiveData for observing changes in the UI.
 */
object NotificationRepository {
    
    private const val MAX_NOTIFICATIONS = 500
    
    private val _notifications = mutableListOf<CapturedNotification>()
    private val _notificationsLiveData = MutableLiveData<List<CapturedNotification>>(emptyList())
    
    private val _isListenerConnected = MutableLiveData(false)
    
    // Counter for generating unique event IDs
    private var _nextEventId: Long = 0L

    /**
     * Observable list of captured notifications (newest first)
     */
    val notifications: LiveData<List<CapturedNotification>> = _notificationsLiveData
    
    /**
     * Observable connection state of the NotificationListenerService
     */
    val isListenerConnected: LiveData<Boolean> = _isListenerConnected
    
    /**
     * Add a new captured notification event to the repository.
     */
    @Synchronized
    fun addNotification(notification: CapturedNotification) {
        val notificationWithId = notification.copy(eventId = _nextEventId++)
        
        // Add at the beginning (newest first)
        _notifications.add(0, notificationWithId)
        
        // Trim to max size (remove oldest from end)
        if (_notifications.size > MAX_NOTIFICATIONS) {
            _notifications.subList(MAX_NOTIFICATIONS, _notifications.size).clear()
        }
        
        // Post updated list
        _notificationsLiveData.postValue(_notifications.toList())
    }
    
    /**
     * Get a notification event by its Android notification key.
     * Note: Multiple events may share the same key (POSTED and REMOVED events).
     */
    @Synchronized
    fun getNotificationByKey(key: String): CapturedNotification? {
        return _notifications.find { it.key == key }
    }
    
    /**
     * Get a notification event by its unique event ID.
     */
    @Synchronized
    fun getNotificationByEventId(eventId: Long): CapturedNotification? {
        return _notifications.find { it.eventId == eventId }
    }
    
    /**
     * Clear all captured notification events and reset the ID counter
     */
    @Synchronized
    fun clear() {
        _notifications.clear()
        _nextEventId = 0L
        _notificationsLiveData.postValue(emptyList())
    }
    
    /**
     * Get the current count of captured notifications
     */
    @Synchronized
    fun getCount(): Int = _notifications.size
    
    /**
     * Update the listener connection state
     */
    fun setListenerConnected(connected: Boolean) {
        _isListenerConnected.postValue(connected)
    }
    
    /**
     * Get all notifications (snapshot)
     */
    @Synchronized
    fun getAllNotifications(): List<CapturedNotification> = _notifications.toList()
}
