package com.maubis.scarlet.base.support.utils

import android.content.Context
import android.text.format.DateFormat
import com.github.bijoysingh.starter.util.DateFormatter
import java.util.*

lateinit var sDateFormat: DateFormatUtils

class DateFormatUtils(context: Context) {
  private val is24HourFormat = DateFormat.is24HourFormat(context)

  fun readableFullTime(timestamp: Long): String = readableTime(
    DateFormatter.Formats.HH_MM_A_DD_MMM_YYYY.format,
    timestamp)

  fun readableTime(format: String, timestamp: Long): String {
    val hourFormatSafe = when {
      is24HourFormat -> format
        .replace("a", "")
        .replace("h", "H")
      else -> format
    }
    return DateFormatter.getDate(
      DateFormat.getBestDateTimePattern(Locale.getDefault(), hourFormatSafe),
      timestamp)
  }

  fun getDateForBackup(): String = DateFormatter.getDate("dd_MMM_yyyy", Calendar.getInstance())
  fun getTimestampForBackup(): String = DateFormatter.getDate("dd_MMM_yyyy HH_mm", Calendar.getInstance())
}