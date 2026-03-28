package com.willbicks.notificationinspector

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.willbicks.notificationinspector.ui.MainViewModel
import com.willbicks.notificationinspector.ui.screens.MainScreen
import com.willbicks.notificationinspector.ui.theme.NotificationInspectorTheme

/**
 * Main activity displaying the list of captured notifications
 * and handling notification listener permission.
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      NotificationInspectorTheme {
        val viewModel: MainViewModel = viewModel()
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
          val observer =
            LifecycleEventObserver { _, event ->
              if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionState()
              }
            }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
          }
        }

        val notifications by viewModel.notifications.collectAsState()
        val connectionState by viewModel.connectionState.collectAsState()
        val isListenerEnabled by viewModel.isListenerEnabled.collectAsState()

        MainScreen(
          notifications = notifications,
          connectionState = connectionState,
          isListenerEnabled = isListenerEnabled,
          onNotificationClick = { notification ->
            startActivity(NotificationDetailActivity.createIntent(this, notification))
          },
          onEnableListener = { openNotificationListenerSettings() },
          onClearAll = { viewModel.clearNotifications() },
        )
      }
    }
  }

  private fun openNotificationListenerSettings() {
    try {
      val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
      startActivity(intent)
    } catch (e: Exception) {
      // Fallback to general settings if specific intent not available
      try {
        val intent = Intent(Settings.ACTION_SETTINGS)
        startActivity(intent)
      } catch (e2: Exception) {
        // Unable to open settings
      }
    }
  }
}
