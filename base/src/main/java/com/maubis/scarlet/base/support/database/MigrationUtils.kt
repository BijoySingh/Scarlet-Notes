package com.maubis.scarlet.base.support.database

import android.content.Context
import com.google.gson.Gson
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.note.NoteMeta
import com.maubis.scarlet.base.core.note.Reminder
import com.maubis.scarlet.base.core.note.getReminder
import com.maubis.scarlet.base.note.reminders.ReminderJob
import com.maubis.scarlet.base.note.saveWithoutSync
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet.Companion.KEY_LIST_VIEW
import com.maubis.scarlet.base.support.ui.KEY_APP_THEME
import com.maubis.scarlet.base.support.ui.KEY_NIGHT_THEME
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.utils.getLastUsedAppVersionCode
import java.io.File
import java.util.*

const val KEY_MIGRATE_THEME = "KEY_MIGRATE_THEME"
const val KEY_MIGRATE_DEFAULT_VALUES = "KEY_MIGRATE_DEFAULT_VALUES"
const val KEY_MIGRATE_REMINDERS = "KEY_MIGRATE_REMINDERS"
const val KEY_MIGRATE_IMAGES = "KEY_MIGRATE_IMAGES"

class Migrator(val context: Context) {

  fun start() {
    runTask(KEY_MIGRATE_THEME) {
      val isNightMode = ApplicationBase.instance.store().get(KEY_NIGHT_THEME, true)
      ApplicationBase.instance.store().put(KEY_APP_THEME, if (isNightMode) Theme.DARK.name else Theme.LIGHT.name)
      ApplicationBase.instance.themeController().notifyChange(context)
    }
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
      ApplicationBase.instance.store().put(KEY_APP_THEME, Theme.DARK.name)
      ApplicationBase.instance.store().put(KEY_LIST_VIEW, true)
    }
  }

  private fun runTask(key: String, task: () -> Unit) {
    if (ApplicationBase.instance.store().get(key, false)) {
      return
    }

    try {
      task()
    } catch (_: Exception) {
    }
    ApplicationBase.instance.store().put(key, true)
  }

  private fun runTaskIf(condition: Boolean, key: String, task: () -> Unit) {
    if (!condition) {
      return
    }
    runTask(key, task)
  }
}