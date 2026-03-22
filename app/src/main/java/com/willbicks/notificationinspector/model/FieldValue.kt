package com.willbicks.notificationinspector.model

sealed interface FieldValue {
  data class StringValue(
    val value: String,
  ) : FieldValue

  data class TimestampValue(
    val millis: Long,
  ) : FieldValue

  data class ColorValue(
    val argb: Int,
  ) : FieldValue

  data class BooleanValue(
    val value: Boolean,
  ) : FieldValue

  data class ListValue(
    val items: List<String>,
  ) : FieldValue

  data object NullValue : FieldValue
}
