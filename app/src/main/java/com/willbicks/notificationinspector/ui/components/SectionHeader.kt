package com.willbicks.notificationinspector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A non-collapsible section header divider for the notification detail view.
 */
@Composable
fun SectionHeader(
  label: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = label,
    style = MaterialTheme.typography.titleSmall,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier =
      modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .padding(horizontal = 16.dp, vertical = 10.dp),
  )
}
