package com.willbicks.notificationinspector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willbicks.notificationinspector.R
import com.willbicks.notificationinspector.model.CapturedNotification
import com.willbicks.notificationinspector.model.NotificationSection
import com.willbicks.notificationinspector.ui.components.FieldRow
import com.willbicks.notificationinspector.ui.components.SectionHeader
import com.willbicks.notificationinspector.ui.theme.Green500
import com.willbicks.notificationinspector.ui.theme.Red500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDetailScreen(
  notification: CapturedNotification,
  onNavigateBack: () -> Unit,
) {
  val isPosted = notification.eventType == CapturedNotification.EventType.POSTED
  val eventColor = if (isPosted) Green500 else Red500

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.notification_detail_title)) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
      )
    },
  ) { paddingValues ->
    LazyColumn(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(paddingValues),
    ) {
      // Header section
      item {
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Event type badge
          Text(
            text = notification.eventType.name,
            color = Color.White,
            fontSize = 12.sp,
            modifier =
              Modifier
                .background(
                  color = eventColor,
                  shape = RoundedCornerShape(4.dp),
                ).padding(horizontal = 8.dp, vertical = 4.dp),
          )

          Spacer(modifier = Modifier.width(12.dp))

          // Package name
          Text(
            text = notification.packageName,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
          )
        }
      }

      // Render the field map
      NotificationSection.entries.forEach { section ->
        val sectionFields = notification.fields[section] ?: return@forEach
        item(key = "section_${section.name}") {
          SectionHeader(label = stringResource(section.titleRes))
        }
        itemsIndexed(
          items = sectionFields,
          key = { index, field -> "field_${section.name}_${index}_${field.label}" },
        ) { index, field ->
          FieldRow(field = field, index = if (section.isOrderedList) index else null)
        }
      }
    }
  }
}
