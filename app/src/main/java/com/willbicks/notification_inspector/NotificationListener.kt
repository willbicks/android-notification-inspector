package com.willbicks.notification_inspector

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.willbicks.notification_inspector.model.CapturedNotification

/**
 * NotificationListenerService that captures all notifications posted or removed
 * and stores them in the NotificationRepository.
 */
class NotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationListener"
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        NotificationRepository.setListenerConnected(true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        NotificationRepository.setListenerConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        Log.d(TAG, "Notification posted: ${sbn.key}")
        
        try {
            val captured = CapturedNotification.fromStatusBarNotification(
                sbn,
                CapturedNotification.EventType.POSTED
            )
            NotificationRepository.addNotification(captured)
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing notification", e)
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification,
        rankingMap: RankingMap,
        reason: Int
    ) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        val reasonStr = getRemovalReasonString(reason)
        Log.d(TAG, "Notification removed: ${sbn.key}, reason: $reasonStr")
        
        try {
            val captured = CapturedNotification.fromStatusBarNotification(
                sbn,
                CapturedNotification.EventType.REMOVED
            )
            NotificationRepository.addNotification(captured)
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing removed notification", e)
        }
    }

    private fun getRemovalReasonString(reason: Int): String {
        return when (reason) {
            REASON_CLICK -> "CLICK"
            REASON_CANCEL -> "CANCEL"
            REASON_CANCEL_ALL -> "CANCEL_ALL"
            REASON_ERROR -> "ERROR"
            REASON_PACKAGE_CHANGED -> "PACKAGE_CHANGED"
            REASON_USER_STOPPED -> "USER_STOPPED"
            REASON_PACKAGE_BANNED -> "PACKAGE_BANNED"
            REASON_APP_CANCEL -> "APP_CANCEL"
            REASON_APP_CANCEL_ALL -> "APP_CANCEL_ALL"
            REASON_LISTENER_CANCEL -> "LISTENER_CANCEL"
            REASON_LISTENER_CANCEL_ALL -> "LISTENER_CANCEL_ALL"
            REASON_GROUP_SUMMARY_CANCELED -> "GROUP_SUMMARY_CANCELED"
            REASON_GROUP_OPTIMIZATION -> "GROUP_OPTIMIZATION"
            REASON_PACKAGE_SUSPENDED -> "PACKAGE_SUSPENDED"
            REASON_PROFILE_TURNED_OFF -> "PROFILE_TURNED_OFF"
            REASON_UNAUTOBUNDLED -> "UNAUTOBUNDLED"
            REASON_CHANNEL_BANNED -> "CHANNEL_BANNED"
            REASON_SNOOZED -> "SNOOZED"
            REASON_TIMEOUT -> "TIMEOUT"
            REASON_CHANNEL_REMOVED -> "CHANNEL_REMOVED"
            REASON_CLEAR_DATA -> "CLEAR_DATA"
            REASON_ASSISTANT_CANCEL -> "ASSISTANT_CANCEL"
            else -> "UNKNOWN($reason)"
        }
    }
}
