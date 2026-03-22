package com.willbicks.notificationinspector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.willbicks.notificationinspector.model.FieldValue
import com.willbicks.notificationinspector.model.NotificationField
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DETAIL_TIME_FORMAT: DateTimeFormatter =
  DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    .withZone(ZoneId.systemDefault())

/**
 * Renders a single notification field row.
 *
 * [NotificationField.Leaf] renders as a key:value row with type-appropriate value rendering.
 * [NotificationField.Branch] renders as a full-width label row with its children indented beneath.
 *
 * [index] is shown as a bracketed prefix on branch rows when non-null.
 *
 * Divider strategy: each row draws a divider immediately after its own header content, before any
 * children. This means:
 *  - A divider always separates a Branch header from its first child.
 *  - A divider always separates a Leaf (or the last child of a Branch) from the next sibling,
 *    because the next sibling draws its own leading divider.
 *  - There is exactly one divider between any two adjacent items — no doubles, no gaps.
 */
@Composable
fun FieldRow(
  field: NotificationField,
  modifier: Modifier = Modifier,
  index: Int? = null,
  indentLevel: Int = 0,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    // Header row: accent bars drawn behind the content via drawBehind, avoiding
    // IntrinsicSize.Min which causes inconsistent measurement in LazyColumn.
    val accentColor = MaterialTheme.colorScheme.primary
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .drawBehind {
            // Draw one 2dp accent bar per indent level, fading with depth.
            // Each bar sits at the left edge of its indent slot (14dp spacer + 2dp bar each level).
            val barWidthPx = 2.dp.toPx()
            val slotWidthPx = 16.dp.toPx() // 14dp spacer + 2dp bar
            repeat(indentLevel) { level ->
              val x = level * slotWidthPx + 14.dp.toPx()
              drawRect(
                color = accentColor.copy(alpha = 1f / (level + 1)),
                topLeft = Offset(x, 0f),
                size = Size(barWidthPx, size.height),
              )
            }
          }.padding(
            start = (indentLevel * 16 + 16).dp,
            end = 16.dp,
            top = 6.dp,
            bottom = 6.dp,
          ),
      verticalAlignment = Alignment.Top,
    ) {
      when (field) {
        is NotificationField.Branch -> {
          // Full-width label with optional bracketed index prefix
          Text(
            text =
              buildAnnotatedString {
                if (index != null) {
                  withStyle(
                    SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant),
                  ) { append("[$index] ") }
                }
                append(field.label)
              },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }

        is NotificationField.Leaf -> {
          // Label column
          Text(
            text = field.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Value column
          SelectionContainer(modifier = Modifier.weight(0.6f)) {
            when (val value = field.value) {
              is FieldValue.StringValue -> {
                Text(
                  text = value.value,
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                )
              }

              is FieldValue.TimestampValue -> {
                Text(
                  text = DETAIL_TIME_FORMAT.format(Instant.ofEpochMilli(value.millis)),
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                )
              }

              is FieldValue.ColorValue -> {
                val luminance = ColorUtils.calculateLuminance(value.argb)
                val borderColor =
                  if (luminance > 0.5) {
                    androidx.compose.ui.graphics.Color.Black
                  } else {
                    androidx.compose.ui.graphics.Color.White
                  }
                val color =
                  androidx.compose.ui.graphics
                    .Color(value.argb or 0xFF000000.toInt())
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier =
                      Modifier
                        .size(14.dp)
                        .background(
                          color = color,
                          shape = RoundedCornerShape(2.dp),
                        ).border(
                          width = 1.dp,
                          color = borderColor,
                          shape = RoundedCornerShape(2.dp),
                        ),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = String.format("#%08X", value.argb),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                  )
                }
              }

              is FieldValue.BooleanValue -> {
                Text(
                  text = if (value.value) "true" else "false",
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  color =
                    if (value.value) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
              }

              is FieldValue.ListValue -> {
                Text(
                  text = value.items.joinToString(", "),
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                )
              }

              is FieldValue.NullValue -> {
                Text(
                  text = "null",
                  style = MaterialTheme.typography.bodySmall,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }
    }

    // Divider after the header row, before any children.
    // This gives exactly one divider between every adjacent pair of visible rows:
    //  - Between a Branch header and its first child (drawn by the branch).
    //  - Between siblings (drawn by the upper sibling after its own header / last child's header).
    // start padding aligns the divider to the accent-bar column at this indent level.
    HorizontalDivider(
      modifier = Modifier.padding(start = (indentLevel * 16).dp),
      color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )

    // Render branch children indented beneath the parent row.
    // No extra dividers here — each child draws its own after-header divider above.
    if (field is NotificationField.Branch) {
      field.children.forEach { child ->
        FieldRow(field = child, indentLevel = indentLevel + 1)
      }
    }
  }
}
