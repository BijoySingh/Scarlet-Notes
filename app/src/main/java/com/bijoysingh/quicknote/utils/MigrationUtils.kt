package com.bijoysingh.quicknote.utils

import android.content.Context
import android.os.AsyncTask
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.activities.sheets.UISettingsOptionsBottomSheet.Companion.KEY_LIST_VIEW
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
const val KEY_MIGRATE_DEFAULT_VALUES = "KEY_MIGRATE_DEFAULT_VALUES"
const val KEY_MIGRATE_CHECKED_LIST = "KEY_MIGRATE_CHECKED_LIST"
const val KEY_MIGRATE_ZERO_NOTES = "KEY_MIGRATE_ZERO_NOTES.v2"

fun migrate(context: Context) {
  if (!userPreferences().get(KEY_MIGRATE_UUID, false)) {
    val tags = HashMap<Int, Tag>()
    for (tag in TagsDB.db.getAll()) {
      if (TextUtils.isNullOrEmpty(tag.uuid)) {
        tag.uuid = RandomHelper.getRandomString(24)
        tag.save()
      }
      tags.put(tag.uid, tag)
    }

    for (note in NotesDB.db.getAll()) {
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
    val notes = NotesDB.db.getByNoteState(arrayOf(NoteState.TRASH.name))
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
    appTheme().notifyUpdate(context)
  }
  if (!userPreferences().get(KEY_MIGRATE_ZERO_NOTES, false)) {
    val note = NotesDB.db.getByID(0)
    if (note != null) {
      NotesDB.db().delete(note)
      NotesDB.db.notifyDelete(note)
      note.uid = null
      note.save(context)
    }
    userPreferences().put(KEY_MIGRATE_ZERO_NOTES, true)
  }
  if (!userPreferences().get(KEY_MIGRATE_CHECKED_LIST, false)) {
    for (note in NotesDB.db.getAll()) {
      note.description = Format.getNote(note.getFormats().sorted())
      note.save(context)
    }
    userPreferences().put(KEY_MIGRATE_CHECKED_LIST, true)
  }
  if (!userPreferences().get(KEY_MIGRATE_DEFAULT_VALUES, false)
      && getLastUsedAppVersionCode() == 0) {
    userPreferences().put(KEY_APP_THEME, Theme.DARK.name)
    userPreferences().put(KEY_LIST_VIEW, true)
    userPreferences().put(KEY_MIGRATE_DEFAULT_VALUES, true)
  }
}

fun removeOlderClips(context: Context) {
  AsyncTask.execute {
    val notes = NotesDB.db().getOldTrashedNotes(Calendar.getInstance().timeInMillis - 1000 * 60 * 60 * 24 * 7)
    for (note in notes) {
      note.delete(context)
    }
  }
}
