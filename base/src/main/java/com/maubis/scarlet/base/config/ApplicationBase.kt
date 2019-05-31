package com.maubis.scarlet.base.config

import android.app.Application
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.maubis.scarlet.base.core.note.NoteImage
import com.maubis.scarlet.base.export.remote.FolderRemoteDatabase
import com.maubis.scarlet.base.note.reminders.ReminderJobCreator
import java.lang.Exception

abstract class ApplicationBase : Application() {
  override fun onCreate() {
    super.onCreate()
    SoLoader.init(this, false)
    try {
      JobManager.create(this).addJobCreator(ReminderJobCreator())
    } catch (exception: Exception) {}
    noteImagesFolder = NoteImage(this)
  }

  companion object {
    lateinit var noteImagesFolder: NoteImage
    lateinit var instance: CoreConfig
    var folderSync: FolderRemoteDatabase? = null
  }
}