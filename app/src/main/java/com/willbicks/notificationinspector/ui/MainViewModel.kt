package com.willbicks.notificationinspector.ui

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.willbicks.notificationinspector.NotificationListener
import com.willbicks.notificationinspector.NotificationRepository
import com.willbicks.notificationinspector.model.CapturedNotification
import com.willbicks.notificationinspector.ui.screens.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
  application: Application,
) : AndroidViewModel(application) {
  private val _isListenerEnabled = MutableStateFlow(false)
  val isListenerEnabled: StateFlow<Boolean> = _isListenerEnabled.asStateFlow()

  val notifications: StateFlow<List<CapturedNotification>> =
    NotificationRepository.notificationsFlow
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
      )

  private val isListenerConnected: StateFlow<Boolean> =
    NotificationRepository.isListenerConnectedFlow
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
      )

  val connectionState: StateFlow<ConnectionState> =
    combine(_isListenerEnabled, isListenerConnected) { enabled, connected ->
      computeConnectionState(connected, enabled)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = ConnectionState.DISABLED,
    )

  private fun computeConnectionState(
    isConnected: Boolean,
    isEnabled: Boolean,
  ): ConnectionState =
    when {
      isConnected -> ConnectionState.CONNECTED
      isEnabled -> ConnectionState.CONNECTING
      else -> ConnectionState.DISABLED
    }

  init {
    checkPermissionState()
  }

  fun checkPermissionState() {
    val context = getApplication<Application>()
    val componentName = ComponentName(context, NotificationListener::class.java)
    val enabledListeners =
      Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
      ) ?: ""

    val enabled = enabledListeners.contains(componentName.flattenToString())
    _isListenerEnabled.value = enabled
  }

  fun clearNotifications() {
    NotificationRepository.clear()
  }
}
