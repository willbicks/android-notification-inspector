package com.willbicks.notificationinspector.model

import androidx.annotation.StringRes
import com.willbicks.notificationinspector.R

/**
 * Sections that group notification fields in the detail view.
 * Each variant carries a string resource ID for its display title.
 */
enum class NotificationSection(
  @StringRes val titleRes: Int,
  val isOrderedList: Boolean = false,
) {
  EVENT_INFO(R.string.detail_section_event_info),
  STATUS_BAR_NOTIFICATION(R.string.detail_section_status_bar_notification),
  NOTIFICATION(R.string.detail_section_notification),
  STYLE(R.string.detail_section_style),
  ACTIONS(R.string.detail_section_actions, isOrderedList = true),
  EXTRAS(R.string.detail_section_extras),
}

/**
 * A node in the notification field tree.
 *
 * [Leaf] holds a single key:value pair.
 * [Branch] groups child fields under a label (e.g. a single action).
 *
 * A field is either a leaf or a branch — never both.
 */
sealed class NotificationField {
  abstract val label: String

  data class Leaf(
    override val label: String,
    val value: FieldValue,
  ) : NotificationField()

  data class Branch(
    override val label: String,
    val children: List<NotificationField>,
  ) : NotificationField()
}
