package com.maubis.scarlet.base.config

import android.app.Application
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.maubis.scarlet.base.core.note.NoteImage
import com.maubis.scarlet.base.export.remote.FolderRemoteDatabase
import com.maubis.scarlet.base.note.reminders.ReminderJobCreator
import com.maubis.scarlet.base.support.ui.ThemeManager
import com.maubis.scarlet.base.support.utils.DateFormatUtils
import com.maubis.scarlet.base.support.utils.ImageCache
import com.maubis.scarlet.base.support.utils.maybeThrow
import com.maubis.scarlet.base.support.utils.sDateFormat

abstract class ApplicationBase : Application() {
  override fun onCreate() {
    super.onCreate()
    sDateFormat = DateFormatUtils(this)
    SoLoader.init(this, false)
    try {
      JobManager.create(this).addJobCreator(ReminderJobCreator())
    } catch (exception: Exception) {
      maybeThrow(exception)
    }
    noteImagesFolder = NoteImage(this)

    // Setup Image Cache
    sImageCache = ImageCache(this)

    // Setup Application Theme
    sAppTheme = ThemeManager()
    sAppTheme.setup(this)
  }

  companion object {
    lateinit var noteImagesFolder: NoteImage
    lateinit var instance: CoreConfig

    lateinit var sImageCache: ImageCache
    lateinit var sAppTheme: ThemeManager

    var folderSync: FolderRemoteDatabase? = null
  }
}