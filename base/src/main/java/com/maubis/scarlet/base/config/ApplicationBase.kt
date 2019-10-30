package com.maubis.scarlet.base.config

import android.app.Application
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
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

    // Preferences
    sAppPreferences = VersionedStore.get(this, "USER_PREFERENCES", 1)

    sDateFormat = DateFormatUtils(this)
    SoLoader.init(this, false)
    try {
      JobManager.create(this).addJobCreator(ReminderJobCreator())
    } catch (exception: Exception) {
      maybeThrow(exception)
    }

    // Setup Image Cache
    sAppImageStorage = NoteImage(this)
    sAppImageCache = ImageCache(this)

    // Setup Application Theme
    sAppTheme = ThemeManager()
    sAppTheme.setup(this)
  }

  companion object {
    lateinit var instance: CoreConfig

    lateinit var sAppImageStorage: NoteImage
    lateinit var sAppImageCache: ImageCache

    lateinit var sAppPreferences: Store

    lateinit var sAppTheme: ThemeManager

    var folderSync: FolderRemoteDatabase? = null
  }
}