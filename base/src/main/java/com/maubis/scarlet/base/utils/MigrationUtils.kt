package com.maubis.scarlet.base.utils

import android.content.Context
import android.os.AsyncTask
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.mark
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet.Companion.KEY_LIST_VIEW
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.ui.KEY_APP_THEME
import com.maubis.scarlet.base.support.ui.KEY_NIGHT_THEME
import com.maubis.scarlet.base.support.ui.Theme
import java.util.*

const val KEY_MIGRATE_UUID = "KEY_MIGRATE_UUID"
const val KEY_MIGRATE_TRASH = "KEY_MIGRATE_TRASH"
const val KEY_MIGRATE_THEME = "KEY_MIGRATE_THEME"
const val KEY_MIGRATE_DEFAULT_VALUES = "KEY_MIGRATE_DEFAULT_VALUES"
const val KEY_MIGRATE_CHECKED_LIST = "KEY_MIGRATE_CHECKED_LIST"
const val KEY_MIGRATE_ZERO_NOTES = "KEY_MIGRATE_ZERO_NOTES.v2"

fun migrate(context: Context) {
  if (!CoreConfig.instance.store().get(KEY_MIGRATE_TRASH, false)) {
    val notes = notesDB.getByNoteState(arrayOf(NoteState.TRASH.name))
    for (note in notes) {
      // Updates the timestamp for the note in trash
      note.mark(context, NoteState.TRASH)
    }
    CoreConfig.instance.store().put(KEY_MIGRATE_TRASH, true)
  }
  if (!CoreConfig.instance.store().get(KEY_MIGRATE_THEME, false)) {
    val isNightMode = CoreConfig.instance.store().get(KEY_NIGHT_THEME, false)
    CoreConfig.instance.store().put(KEY_APP_THEME, if (isNightMode) Theme.DARK.name else Theme.LIGHT.name)
    CoreConfig.instance.store().put(KEY_MIGRATE_THEME, true)
    CoreConfig.instance.themeController().notifyChange(context)
  }
  if (!CoreConfig.instance.store().get(KEY_MIGRATE_ZERO_NOTES, false)) {
    val note = notesDB.getByID(0)
    if (note != null) {
      notesDB.database().delete(note)
      notesDB.notifyDelete(note)
      note.uid = null
      note.save(context)
    }
    CoreConfig.instance.store().put(KEY_MIGRATE_ZERO_NOTES, true)
  }
  if (!CoreConfig.instance.store().get(KEY_MIGRATE_CHECKED_LIST, false)) {
    for (note in notesDB.getAll()) {
      note.description = FormatBuilder().getDescription(note.getFormats().sorted())
      note.save(context)
    }
    CoreConfig.instance.store().put(KEY_MIGRATE_CHECKED_LIST, true)
  }
  if (!CoreConfig.instance.store().get(KEY_MIGRATE_DEFAULT_VALUES, false)
      && getLastUsedAppVersionCode() == 0) {
    CoreConfig.instance.store().put(KEY_APP_THEME, Theme.DARK.name)
    CoreConfig.instance.store().put(KEY_LIST_VIEW, true)
    CoreConfig.instance.store().put(KEY_MIGRATE_DEFAULT_VALUES, true)
  }
}

fun removeOlderClips(context: Context) {
  AsyncTask.execute {
    val notes = notesDB.database().getOldTrashedNotes(Calendar.getInstance().timeInMillis - 1000 * 60 * 60 * 24 * 7)
    for (note in notes) {
      note.delete(context)
    }
  }
}
