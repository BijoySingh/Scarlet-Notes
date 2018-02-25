package com.bijoysingh.quicknote.utils

import android.content.Context
import com.bijoysingh.quicknote.activities.external.ExportableNote
import com.bijoysingh.quicknote.activities.external.ExportableTag
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType
import com.github.bijoysingh.starter.util.RandomHelper
import com.github.bijoysingh.starter.util.TextUtils
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList

fun getNewNoteUUID() = RandomHelper.getRandomString(24)

/**
 * Generate blank note with default configuration
 */
fun genEmptyNote(): Note {
  val note = Note()
  note.uuid = getNewNoteUUID()
  note.state = NoteState.DEFAULT.name
  note.timestamp = Calendar.getInstance().timeInMillis
  note.updateTimestamp = note.timestamp
  note.color = -0xff8695
  return note
}

/**
 * Generate blank note with color
 */
fun genEmptyNote(color: Int): Note {
  val note = genEmptyNote()
  note.color = color
  return note
}

/**
 * Generate blank note from basic title and description
 */
fun genEmptyNote(title: String, description: String): Note {
  val note = genEmptyNote()
  val formats = ArrayList<Format>()
  if (!TextUtils.isNullOrEmpty(title)) {
    formats.add(Format(FormatType.HEADING, title))
  }
  formats.add(Format(FormatType.TEXT, description))
  note.description = Format.getNote(formats)
  return note
}

/**
 * Generate blank note from basic title and description
 */
fun genEmptyNote(title: String, formatSource: List<Format>): Note {
  val note = genEmptyNote()
  val formats = ArrayList<Format>()
  if (!TextUtils.isNullOrEmpty(title)) {
    formats.add(Format(FormatType.HEADING, title))
  }
  formats.addAll(formatSource)
  note.description = Format.getNote(formats)
  return note
}

fun genImportFromKeep(description: String): List<Format> {
  val randomDelimiter = "-+-" + RandomHelper.getRandom() + "-+-"
  var delimitered = description.replace("(^|\n)\\s*\\[\\s\\]\\s*".toRegex(), randomDelimiter + "[ ]")
  delimitered = delimitered.replace("(^|\n)\\s*\\[x\\]\\s*".toRegex(), randomDelimiter + "[x]")

  val items = delimitered.split(randomDelimiter)
  val formats = ArrayList<Format>()
  for (item in items) {
    when {
      item.startsWith("[ ]") -> formats.add(Format(FormatType.CHECKLIST_UNCHECKED, item.removePrefix("[ ]")))
      item.startsWith("[x]") -> formats.add(Format(FormatType.CHECKLIST_CHECKED, item.removePrefix("[x]")))
      !item.isBlank() -> formats.add(Format(FormatType.TEXT, item))
    }
  }
  return formats
}

/**
 * Generate blank note from imported note
 */
fun genImportedNote(context: Context, exportableNote: ExportableNote): Note {
  val existingNote = Note.db(context).getByUUID(exportableNote.uuid)
  val note = existingNote ?: genEmptyNote()
  note.uuid = exportableNote.uuid
  note.color = exportableNote.color
  note.description = exportableNote.description
  note.timestamp = exportableNote.timestamp
  note.updateTimestamp = note.timestamp
  for (index in 0 until exportableNote.tags.length()) {
    try {
      val tag = ExportableTag.getBestPossibleTagObject(
          context,
          exportableNote.tags.getJSONObject(index))
      note.toggleTag(tag)
    } catch (exception: JSONException) {
      // Ignore this exception
    }
  }
  note.save(context)
  return note
}

/**
 * Copies the original note to a new Note object
 *
 * @param reference the initial note
 * @return the new note
 */
fun copyNote(reference: Note): Note {
  val note = genEmptyNote()
  return note.copyNote(reference)
}