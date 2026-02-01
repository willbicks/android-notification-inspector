package com.willbicks.notification_inspector

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.willbicks.notification_inspector.ui.MainViewModel
import com.willbicks.notification_inspector.ui.screens.ConnectionState
import com.willbicks.notification_inspector.ui.screens.MainScreen
import com.willbicks.notification_inspector.ui.theme.NotificationInspectorTheme

/**
 * Main activity displaying the list of captured notifications
 * and handling notification listener permission.
 */
class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NotificationInspectorTheme {
                viewModel = viewModel()
                
                val notifications by viewModel.notifications.observeAsState(emptyList())
                val connectionState by viewModel.connectionState.observeAsState(ConnectionState.DISABLED)
                val isListenerEnabled by viewModel.isListenerEnabled.observeAsState(false)

                MainScreen(
                    notifications = notifications,
                    connectionState = connectionState,
                    isListenerEnabled = isListenerEnabled,
                    onNotificationClick = { notification ->
                        startActivity(NotificationDetailActivity.createIntent(this, notification))
                    },
                    onEnableListener = { openNotificationListenerSettings() },
                    onClearAll = { viewModel.clearNotifications() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission state when returning to the activity
        if (::viewModel.isInitialized) {
            viewModel.checkPermissionState()
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
