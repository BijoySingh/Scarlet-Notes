package com.maubis.scarlet.base.core.note

import com.github.bijoysingh.starter.util.RandomHelper
import com.github.bijoysingh.starter.util.TextUtils
import com.google.gson.Gson
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import java.util.*

fun generateUUID() = UUID.randomUUID().toString()

class NoteBuilder {
  /**
   * Generate blank note with default configuration
   */
  fun emptyNote(): Note {
    val note = Note()
    note.uuid = generateUUID()
    note.state = NoteState.DEFAULT.name
    note.timestamp = Calendar.getInstance().timeInMillis
    note.updateTimestamp = note.timestamp
    note.color = -0xff8695
    return note
  }

  /**
   * Generate blank note with color
   */
  fun emptyNote(color: Int): Note {
    val note = emptyNote()
    note.color = color
    return note
  }

  /**
   * Generate blank note from basic title and description
   */
  fun gen(title: String, description: String): Note {
    val note = emptyNote()
    val formats = ArrayList<Format>()
    if (!TextUtils.isNullOrEmpty(title)) {
      formats.add(Format(FormatType.HEADING, title))
    }
    formats.add(Format(FormatType.TEXT, description))
    note.description = FormatBuilder().getDescription(formats)
    return note
  }

  /**
   * Generate blank note from basic title and description
   */
  fun gen(title: String, formatSource: List<Format>): Note {
    val note = emptyNote()
    val formats = ArrayList<Format>()
    if (!TextUtils.isNullOrEmpty(title)) {
      formats.add(Format(FormatType.HEADING, title))
    }
    formats.addAll(formatSource)
    note.description = FormatBuilder().getDescription(formats)
    return note
  }

  fun genFromKeep(content: String): List<Format> {
    val randomDelimiter = "-+-" + RandomHelper.getRandom() + "-+-"
    var delimitered = content.replace("(^|\n)\\s*\\[\\s\\]\\s*".toRegex(), randomDelimiter + "[ ]")
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

  fun copy(noteContainer: INoteContainer): Note {
    val note = emptyNote()
    note.uuid = noteContainer.uuid()
    note.description = noteContainer.description()
    note.timestamp = noteContainer.timestamp()
    note.updateTimestamp = Math.max(note.updateTimestamp, note.timestamp)
    note.color = noteContainer.color()
    note.state = noteContainer.state()
    note.locked = noteContainer.locked()
    note.pinned = noteContainer.pinned()
    note.tags = noteContainer.tags()
    note.meta = Gson().toJson(noteContainer.meta())
    return note
  }

  fun copy(reference: Note): Note {
    val note = emptyNote()
    note.uid = reference.uid
    note.uuid = reference.uuid
    note.state = reference.state
    note.description = reference.description
    note.timestamp = reference.timestamp
    note.updateTimestamp = reference.updateTimestamp
    note.color = reference.color
    note.tags = reference.tags
    note.pinned = reference.pinned
    note.locked = reference.locked
    note.meta = reference.meta
    return note
  }
}