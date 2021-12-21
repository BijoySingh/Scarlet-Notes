package com.maubis.scarlet.base.support.database

import android.content.Context
import com.google.gson.Gson
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.note.NoteMeta
import com.maubis.scarlet.base.core.note.Reminder
import com.maubis.scarlet.base.core.note.getReminder
import com.maubis.scarlet.base.note.reminders.ReminderJob
import com.maubis.scarlet.base.note.saveWithoutSync
import com.maubis.scarlet.base.settings.sheet.sUIUseGridView
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.sThemeLabel
import com.maubis.scarlet.base.support.utils.getLastUsedAppVersionCode
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

const val KEY_MIGRATE_DEFAULT_VALUES = "KEY_MIGRATE_DEFAULT_VALUES"
const val KEY_MIGRATE_REMINDERS = "KEY_MIGRATE_REMINDERS"
const val KEY_MIGRATE_IMAGES = "KEY_MIGRATE_IMAGES"
const val KEY_MIGRATE_TO_GDRIVE_DATABASE = "KEY_MIGRATE_TO_GDRIVE_DATABASE_v2"

class Migrator(val context: Context) {

  fun start() {
    runTask(key = KEY_MIGRATE_REMINDERS) {
      val notes = notesDb.getAll()
      for (note in notes) {
        val legacyReminder = note.getReminder()
        if (legacyReminder !== null) {
          val reminder = Reminder(0, legacyReminder.alarmTimestamp, legacyReminder.interval)
          if (legacyReminder.alarmTimestamp < Calendar.getInstance().timeInMillis) {
            continue
          }

          val meta = NoteMeta()
          val uid = ReminderJob.scheduleJob(note.uuid, reminder)
          reminder.uid = uid
          if (uid == -1) {
            continue
          }

          meta.reminderV2 = reminder
          note.meta = Gson().toJson(meta)
          note.saveWithoutSync(context)
        }
      }
    }
    runTask(KEY_MIGRATE_IMAGES) {
      File(context.cacheDir, "images").renameTo(File(context.filesDir, "images"))
    }
    runTaskIf(
      getLastUsedAppVersionCode() == 0,
      KEY_MIGRATE_DEFAULT_VALUES) {
      sThemeLabel = Theme.DARK.name
      sUIUseGridView = true
    }

    runTask(KEY_MIGRATE_TO_GDRIVE_DATABASE) {
      GlobalScope.launch {
        val remoteDatabaseState = ApplicationBase.instance.remoteDatabaseState()
        ApplicationBase.instance.notesDatabase().getAll().forEach {
          remoteDatabaseState.notifyInsert(it) {}
        }
        ApplicationBase.instance.tagsDatabase().getAll().forEach {
          remoteDatabaseState.notifyInsert(it) {}
        }
        ApplicationBase.instance.foldersDatabase().getAll().forEach {
          remoteDatabaseState.notifyInsert(it) {}
        }
      }
    }
  }

  private fun runTask(key: String, task: () -> Unit) {
    if (sAppPreferences.get(key, false)) {
      return
    }

    try {
      task()
    } catch (exception: Exception) {
      maybeThrow(exception)
    }
    sAppPreferences.put(key, true)
  }

  private fun runTaskIf(condition: Boolean, key: String, task: () -> Unit) {
    if (!condition) {
      return
    }
    runTask(key, task)
  }
}