package com.bijoysingh.quicknote

import android.app.Application
import com.evernote.android.job.JobManager
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.note.reminders.ReminderJobCreator

class MaterialNotes : Application() {

  override fun onCreate() {
    super.onCreate()
    CoreConfig.instance = MaterialNoteConfig(this)
    CoreConfig.instance.themeController().setup(this)
    JobManager.create(this).addJobCreator(ReminderJobCreator())
  }
}