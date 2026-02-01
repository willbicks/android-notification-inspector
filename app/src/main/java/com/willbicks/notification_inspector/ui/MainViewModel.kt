package com.willbicks.notification_inspector.ui

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.willbicks.notification_inspector.NotificationListener
import com.willbicks.notification_inspector.NotificationRepository
import com.willbicks.notification_inspector.model.CapturedNotification
import com.willbicks.notification_inspector.ui.screens.ConnectionState

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val notifications: LiveData<List<CapturedNotification>> = NotificationRepository.notifications

    private val _isListenerEnabled = MutableLiveData(false)
    val isListenerEnabled: LiveData<Boolean> = _isListenerEnabled

    private val _connectionState = MediatorLiveData<ConnectionState>().apply {
        addSource(NotificationRepository.isListenerConnected) { isConnected ->
            value = computeConnectionState(isConnected, _isListenerEnabled.value ?: false)
        }
        addSource(_isListenerEnabled) { enabled ->
            value = computeConnectionState(
                NotificationRepository.isListenerConnected.value ?: false,
                enabled
            )
        }
    }
    val connectionState: LiveData<ConnectionState> = _connectionState

    private fun computeConnectionState(isConnected: Boolean, isEnabled: Boolean): ConnectionState {
        return when {
            isConnected -> ConnectionState.CONNECTED
            isEnabled -> ConnectionState.CONNECTING
            else -> ConnectionState.DISABLED
        }
    }

    init {
        checkPermissionState()
    }

    fun checkPermissionState() {
        val context = getApplication<Application>()
        val componentName = ComponentName(context, NotificationListener::class.java)
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: ""
        
        val enabled = enabledListeners.contains(componentName.flattenToString())
        _isListenerEnabled.value = enabled
    }

    fun clearNotifications() {
        NotificationRepository.clear()
    }

    fun getNotificationByEventId(eventId: Long): CapturedNotification? {
        return NotificationRepository.getNotificationByEventId(eventId)
    }

    fun getNotificationCount(): Int {
        return NotificationRepository.getCount()
    }
}
