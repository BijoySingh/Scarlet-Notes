package com.maubis.scarlet.base.support.utils

import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.github.bijoysingh.starter.util.DateFormatter
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.core.note.isUnsaved
import com.maubis.scarlet.base.main.sheets.ExceptionBottomSheet
import com.maubis.scarlet.base.note.unsafeSave_INTERNAL_USE_ONLY
import com.maubis.scarlet.base.support.sheets.openSheet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val KEY_INTERNAL_LOG_TRACES_TO_NOTE = "internal_log_traces_to_note"
var sInternalLogTracesToNote: Boolean
  get() = instance.store().get(KEY_INTERNAL_LOG_TRACES_TO_NOTE, false)
  set(value) = instance.store().put(KEY_INTERNAL_LOG_TRACES_TO_NOTE, value)

const val KEY_INTERNAL_SHOW_TRACES_IN_SHEET = "internal_show_traces_in_sheet"
var sInternalShowTracesInSheet: Boolean
  get() = instance.store().get(KEY_INTERNAL_SHOW_TRACES_IN_SHEET, false)
  set(value) = instance.store().put(KEY_INTERNAL_SHOW_TRACES_IN_SHEET, value)

const val KEY_INTERNAL_THROW_ON_EXCEPTION = "internal_throw_on_exception"
var sInternalThrowOnException: Boolean
  get() = instance.store().get(KEY_INTERNAL_THROW_ON_EXCEPTION, false)
  set(value) = instance.store().put(KEY_INTERNAL_THROW_ON_EXCEPTION, value)

const val KEY_INTERNAL_THROWN_EXCEPTION_COUNT = "internal_thrown_exception_count"
var sInternalThrownExceptionCount: Int
  get() = instance.store().get(KEY_INTERNAL_THROWN_EXCEPTION_COUNT, 0)
  set(value) = instance.store().put(KEY_INTERNAL_THROWN_EXCEPTION_COUNT, value)

/**
 * Throws in debug builds and stores the log trace to a fixed note in case of 'internal debug mode'.
 */
fun maybeThrow(activity: AppCompatActivity, thrownException: Exception) {
  if (sInternalShowTracesInSheet) {
    openSheet(activity, ExceptionBottomSheet().apply { this.exception = thrownException })
  }
  maybeThrow(thrownException)
}


/**
 * Throws in debug builds and stores the log trace to a fixed note in case of 'internal debug mode'.
 */
fun maybeThrow(exception: Exception) {
  if (sInternalLogTracesToNote) {
    storeToDebugNote(Log.getStackTraceString(exception))
  }

  if (sInternalThrowOnException) {
    sInternalThrownExceptionCount += 1
    if (sInternalThrownExceptionCount <= 5) {
      GlobalScope.launch {
        SystemClock.sleep(1000)
        throw exception
      }
    }

    sInternalThrownExceptionCount = 0
    sInternalThrowOnException = false
  }
}

/**
 * Throws in debug builds and stores the log trace to a fixed note in case of 'internal debug mode'.
 */
fun maybeThrow(message: String) {
  maybeThrow(IllegalStateException(message))
}

/**
 * Throws in debug builds and stores the log trace to a fixed note in case of 'internal debug mode'.
 * Else returns the provided value
 */
fun <DataType> throwOrReturn(message: String, result: DataType): DataType {
  return throwOrReturn(IllegalStateException(message), result)
}

/**
 * Throws in debug builds and stores the log trace to a fixed note in case of 'internal debug mode'.
 * Else returns the provided value
 */
fun <DataType> throwOrReturn(exception: Exception, result: DataType): DataType {
  maybeThrow(exception)
  return result
}


private fun storeToDebugNote(trace: String) {
  GlobalScope.launch {
    storeToDebugNoteSync(trace)
  }
}

const val EXCEPTION_NOTE_KEY = "debug-note"
const val EXCEPTION_NOTE_NUM_DATA_PER_EXCEPTION = 4
const val EXCEPTION_NOTE_MAX_EXCEPTIONS = 20

@Synchronized
private fun storeToDebugNoteSync(trace: String) {
  val note = instance.notesDatabase().getByUUID(EXCEPTION_NOTE_KEY)
      ?: NoteBuilder().emptyNote().apply {
        uuid = EXCEPTION_NOTE_KEY
        disableBackup = true
      }

  val initialFormats = note.getFormats().toMutableList()
  if (note.isUnsaved() || initialFormats.isEmpty()) {
    initialFormats.add(Format(FormatType.HEADING, "Note Exceptions"))
  }

  val additionalFormats = emptyList<Format>().toMutableList()
  additionalFormats.add(Format(FormatType.SUB_HEADING, "Exception"))
  additionalFormats.add(Format(
      FormatType.QUOTE,
      "Throw at ${DateFormatter.getDate(System.currentTimeMillis())}"))
  additionalFormats.add(Format(
      FormatType.CODE,
      trace))
  additionalFormats.add(Format(FormatType.SEPARATOR))

  val maxFormatCount = 1 + EXCEPTION_NOTE_MAX_EXCEPTIONS * EXCEPTION_NOTE_NUM_DATA_PER_EXCEPTION
  if (initialFormats.size > maxFormatCount) {
    initialFormats.subList(0, maxFormatCount)
  }

  initialFormats.addAll(1, additionalFormats)
  note.description = FormatBuilder().getDescription(initialFormats)
  note.unsafeSave_INTERNAL_USE_ONLY()
}