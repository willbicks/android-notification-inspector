package com.willbicks.notificationinspector.extraction

import android.app.Notification
import android.os.Parcelable
import android.service.notification.StatusBarNotification
import android.widget.RemoteViews
import com.willbicks.notificationinspector.model.CapturedNotification
import com.willbicks.notificationinspector.model.CapturedNotification.EventType
import com.willbicks.notificationinspector.model.FieldValue
import com.willbicks.notificationinspector.model.NotificationField
import com.willbicks.notificationinspector.model.NotificationSection

/**
 * Stateless extractor that converts a [StatusBarNotification] into a
 * [CapturedNotification] with a structured field map for the detail view.
 */
object NotificationFieldExtractor {
  /**
   * Extract a [CapturedNotification] with its field map from a [StatusBarNotification].
   */
  fun extract(
    sbn: StatusBarNotification,
    eventType: EventType,
  ): CapturedNotification {
    val notification = sbn.notification
    val extras = notification.extras
    val captureTime = System.currentTimeMillis()

    val fields =
      buildMap<NotificationSection, List<NotificationField>> {
        put(NotificationSection.EVENT_INFO, extractEventInfo(captureTime, eventType))
        put(NotificationSection.STATUS_BAR_NOTIFICATION, extractSbnFields(sbn))
        put(NotificationSection.NOTIFICATION, extractNotificationFields(notification))
        put(NotificationSection.STYLE, extractStyle(notification, extras))
        extractActions(notification.actions)?.let { put(NotificationSection.ACTIONS, it) }
        extractExtras(extras)?.let { put(NotificationSection.EXTRAS, it) }
      }

    return CapturedNotification(
      captureTime = captureTime,
      eventType = eventType,
      key = sbn.key,
      packageName = sbn.packageName,
      postTime = sbn.postTime,
      title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
      text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
      fields = fields,
    )
  }

  // ---------------------------------------------------------------------------
  // Section builders
  // ---------------------------------------------------------------------------

  private fun extractEventInfo(
    captureTime: Long,
    eventType: EventType,
  ): List<NotificationField> =
    listOfNotNull(
      leaf("Event Type", eventType.name),
      leaf("Capture Time", FieldValue.TimestampValue(captureTime)),
    )

  private fun extractSbnFields(sbn: StatusBarNotification): List<NotificationField> =
    listOfNotNull(
      leaf("Key", sbn.key),
      leaf("ID", sbn.id.toString()),
      leaf("Tag", sbn.tag),
      leaf("Package", sbn.packageName),
      leaf("Op Package", sbn.opPkg),
      leaf("UID", sbn.uid.toString()),
      leaf("User", sbn.user.toString()),
      leaf("Group Key", sbn.groupKey),
      leaf("Override Group Key", sbn.overrideGroupKey),
      leaf("Post Time", FieldValue.TimestampValue(sbn.postTime)),
      leaf("Is Ongoing", FieldValue.BooleanValue(sbn.isOngoing)),
      leaf("Is Clearable", FieldValue.BooleanValue(sbn.isClearable)),
      leaf("Is Group", FieldValue.BooleanValue(sbn.isGroup)),
      leaf("Is App Group", FieldValue.BooleanValue(sbn.isAppGroup)),
    )

  private fun extractNotificationFields(notification: Notification): List<NotificationField> {
    val flagNames = parseFlags(notification.flags)
    return listOfNotNull(
      leaf("When", FieldValue.TimestampValue(notification.`when`)),
      leaf("Flags", notification.flags.toString()),
      if (flagNames.isNotEmpty()) leaf("Flag Names", FieldValue.ListValue(flagNames)) else null,
      @Suppress("DEPRECATION")
      leaf("Priority", getPriorityName(notification.priority)),
      leaf("Visibility", getVisibilityName(notification.visibility)),
      leaf("Color", FieldValue.ColorValue(notification.color)),
      leaf("Category", notification.category),
      leaf("Channel ID", notification.channelId),
      leaf("Group", notification.group),
      leaf("Sort Key", notification.sortKey),
      leaf("Ticker Text", notification.tickerText?.toString()),
      leaf("Number", notification.number.toString()),
    )
  }

  private fun extractStyle(
    notification: Notification,
    extras: android.os.Bundle?,
  ): List<NotificationField> {
    val styleType = extras?.getString(Notification.EXTRA_TEMPLATE)
    val isDecoratedCustomView =
      styleType?.contains("DecoratedCustomViewStyle") == true ||
        styleType?.contains("DecoratedMediaCustomViewStyle") == true

    @Suppress("DEPRECATION")
    val hasContentView = notification.contentView != null

    @Suppress("DEPRECATION")
    val hasBigContentView = notification.bigContentView != null

    @Suppress("DEPRECATION")
    val hasHeadsUpContentView = notification.headsUpContentView != null

    return listOfNotNull(
      leaf("Style Type", styleType ?: "(standard/unknown)"),
      leaf("Is Decorated Custom View Style", FieldValue.BooleanValue(isDecoratedCustomView)),
      leaf("Has contentView", FieldValue.BooleanValue(hasContentView)),
      leaf("Has bigContentView", FieldValue.BooleanValue(hasBigContentView)),
      leaf("Has headsUpContentView", FieldValue.BooleanValue(hasHeadsUpContentView)),
    )
  }

  private fun extractActions(actions: Array<Notification.Action>?): List<NotificationField>? {
    if (actions.isNullOrEmpty()) return null
    return actions.map { action ->
      val title = action.title?.toString() ?: "(no title)"
      val hasRemoteInput = action.remoteInputs?.isNotEmpty() == true
      NotificationField.Branch(
        label = title,
        children =
          listOfNotNull(
            leaf("Title", action.title?.toString()),
            leaf("Has RemoteInput", FieldValue.BooleanValue(hasRemoteInput)),
          ),
      )
    }
  }

  private fun extractExtras(extras: android.os.Bundle?): List<NotificationField>? {
    if (extras == null) return null
    val keys = extras.keySet() ?: return null
    if (keys.isEmpty()) return null

    return keys.sorted().mapNotNull { key ->
      try {
        val value = extras.get(key)
        leaf(key, extraValueToFieldValue(value))
      } catch (e: Exception) {
        leaf(key, FieldValue.StringValue("(error reading: ${e.message})"))
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Convenience helpers
  // ---------------------------------------------------------------------------

  private fun leaf(
    label: String,
    value: String?,
  ): NotificationField.Leaf? = value?.let { NotificationField.Leaf(label, FieldValue.StringValue(it)) }

  private fun leaf(
    label: String,
    value: FieldValue,
  ): NotificationField.Leaf = NotificationField.Leaf(label, value)

  // ---------------------------------------------------------------------------
  // Value formatting helpers
  // ---------------------------------------------------------------------------

  /**
   * Maps a bundle extra value to the most appropriate [FieldValue] subtype.
   *
   * - [Boolean] → [FieldValue.BooleanValue]
   * - [CharSequence] / [String] → [FieldValue.StringValue]
   * - [Array] or [ArrayList] whose elements are all [CharSequence] → [FieldValue.ListValue]
   * - Everything else → [FieldValue.StringValue] via [describeExtraValueAsString]
   */
  private fun extraValueToFieldValue(value: Any?): FieldValue {
    if (value == null) return FieldValue.NullValue

    return when {
      value is Boolean -> {
        FieldValue.BooleanValue(value)
      }

      value is CharSequence -> {
        FieldValue.StringValue(value.toString())
      }

      value is IntArray -> {
        FieldValue.ListValue(value.map { it.toString() })
      }

      value is Array<*> && value.isNotEmpty() && value.all { it is CharSequence } -> {
        FieldValue.ListValue(value.map { it.toString() })
      }

      value is ArrayList<*> && value.isNotEmpty() && value.all { it is CharSequence } -> {
        FieldValue.ListValue(value.map { it.toString() })
      }

      else -> {
        FieldValue.StringValue(describeExtraValueAsString(value))
      }
    }
  }

  private fun describeExtraValueAsString(value: Any?): String {
    if (value == null) return "(null)"
    return when (value) {
      is ByteArray -> {
        "(ByteArray, ${value.size} bytes)"
      }

      is Array<*> -> {
        "(Array, ${value.size} items): ${value.take(5).joinToString { it?.toString() ?: "null" }}"
      }

      is Parcelable -> {
        when (value) {
          is RemoteViews -> {
            "(RemoteViews: pkg=${value.`package`}, layout=0x${Integer.toHexString(value.layoutId)})"
          }

          else -> {
            "(${value.javaClass.simpleName})"
          }
        }
      }

      else -> {
        value.toString()
      }
    }
  }

  private fun parseFlags(flags: Int): List<String> {
    val result = mutableListOf<String>()
    @Suppress("DEPRECATION")
    if (flags and Notification.FLAG_SHOW_LIGHTS != 0) result.add("SHOW_LIGHTS")
    if (flags and Notification.FLAG_ONGOING_EVENT != 0) result.add("ONGOING_EVENT")
    if (flags and Notification.FLAG_INSISTENT != 0) result.add("INSISTENT")
    if (flags and Notification.FLAG_ONLY_ALERT_ONCE != 0) result.add("ONLY_ALERT_ONCE")
    if (flags and Notification.FLAG_AUTO_CANCEL != 0) result.add("AUTO_CANCEL")
    if (flags and Notification.FLAG_NO_CLEAR != 0) result.add("NO_CLEAR")
    if (flags and Notification.FLAG_FOREGROUND_SERVICE != 0) result.add("FOREGROUND_SERVICE")
    @Suppress("DEPRECATION")
    if (flags and Notification.FLAG_HIGH_PRIORITY != 0) result.add("HIGH_PRIORITY")
    if (flags and Notification.FLAG_LOCAL_ONLY != 0) result.add("LOCAL_ONLY")
    if (flags and Notification.FLAG_GROUP_SUMMARY != 0) result.add("GROUP_SUMMARY")
    if (flags and Notification.FLAG_BUBBLE != 0) result.add("BUBBLE")
    return result
  }

  @Suppress("DEPRECATION")
  private fun getPriorityName(priority: Int): String =
    when (priority) {
      Notification.PRIORITY_MIN -> "MIN"
      Notification.PRIORITY_LOW -> "LOW"
      Notification.PRIORITY_DEFAULT -> "DEFAULT"
      Notification.PRIORITY_HIGH -> "HIGH"
      Notification.PRIORITY_MAX -> "MAX"
      else -> "UNKNOWN ($priority)"
    }

  private fun getVisibilityName(visibility: Int): String =
    when (visibility) {
      Notification.VISIBILITY_PUBLIC -> "PUBLIC"
      Notification.VISIBILITY_PRIVATE -> "PRIVATE"
      Notification.VISIBILITY_SECRET -> "SECRET"
      else -> "UNKNOWN ($visibility)"
    }
}
