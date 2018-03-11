package com.bijoysingh.quicknote.utils

import android.content.Context
import android.os.AsyncTask
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.utils.*
import com.bijoysingh.quicknote.formats.Format
import com.github.bijoysingh.starter.util.RandomHelper
import com.github.bijoysingh.starter.util.TextUtils
import java.util.*

const val KEY_MIGRATE_UUID = "KEY_MIGRATE_UUID"
const val KEY_MIGRATE_TRASH = "KEY_MIGRATE_TRASH"
const val KEY_MIGRATE_THEME = "KEY_MIGRATE_THEME"
const val KEY_MIGRATE_CHECKED_LIST = "KEY_MIGRATE_CHECKED_LIST"
const val KEY_MIGRATE_ZERO_NOTES = "MIGRATE_ZERO_NOTES"

fun migrate(context: Context) {
  if (!userPreferences().get(KEY_MIGRATE_UUID, false)) {
    val tags = HashMap<Int, Tag>()
    for (tag in Tag.db().all) {
      if (TextUtils.isNullOrEmpty(tag.uuid)) {
        tag.uuid = RandomHelper.getRandomString(24)
        tag.save(context)
      }
      tags.put(tag.uid, tag)
    }

    for (note in Note.db().all) {
      var saveNote = false
      if (TextUtils.isNullOrEmpty(note.uuid)) {
        note.uuid = RandomHelper.getRandomString(24)
        saveNote = true
      }
      if (!TextUtils.isNullOrEmpty(note.tags)) {
        val tagIDs = note.getTagIDs()
        note.tags = ""
        for (tagID in tagIDs) {
          val tag = tags.get(tagID)
          if (tag !== null) {
            note.toggleTag(tag)
          }
        }
        saveNote = true
      }
      if (saveNote) {
        note.saveWithoutSync(context)
      }
    }
    userPreferences().put(KEY_MIGRATE_UUID, true)
  }
  if (!userPreferences().get(KEY_MIGRATE_TRASH, false)) {
    val notes = Note.db().getByNoteState(arrayOf(NoteState.TRASH.name))
    for (note in notes) {
      // Updates the timestamp for the note in trash
      note.mark(context, NoteState.TRASH)
    }
    userPreferences().put(KEY_MIGRATE_TRASH, true)
  }

  if (!userPreferences().get(KEY_MIGRATE_THEME, false)) {
    val isNightMode = userPreferences().get(KEY_NIGHT_THEME, false)
    userPreferences().put(KEY_APP_THEME, if (isNightMode) Theme.DARK.name else Theme.LIGHT.name)
    userPreferences().put(KEY_MIGRATE_THEME, true)
  }

  if (!userPreferences().get(KEY_MIGRATE_ZERO_NOTES, false)) {
    val note = Note.db().getByID(0)
    if (note != null) {
      Note.db().delete(note)
      note.uid = null
      note.save(context)
    }
    userPreferences().put(KEY_MIGRATE_ZERO_NOTES, true)
  }
  if (!userPreferences().get(KEY_MIGRATE_CHECKED_LIST, false)) {
    for (note in Note.db().all) {
      note.description = Format.getNote(note.getFormats().sorted())
      note.save(context)
    }
    userPreferences().put(KEY_MIGRATE_CHECKED_LIST, true)
  }
}

fun removeOlderClips(context: Context) {
  AsyncTask.execute {
    val notes = Note.db().getByNoteState(arrayOf(NoteState.TRASH.name))
    val timestamp = Calendar.getInstance().timeInMillis - 1000 * 60 * 60 * 24 * 7
    for (note in notes) {
      if (note.updateTimestamp < timestamp) {
        note.delete(context)
      }
    }
  }
}
