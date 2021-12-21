package com.maubis.scarlet.base.config

import android.app.Application
import androidx.biometric.BiometricManager
import com.evernote.android.job.JobManager
import com.facebook.soloader.SoLoader
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.maubis.scarlet.base.core.note.NoteImage
import com.maubis.scarlet.base.export.remote.FolderRemoteDatabase
import com.maubis.scarlet.base.note.reminders.ReminderJobCreator
import com.maubis.scarlet.base.support.ui.ThemeManager
import com.maubis.scarlet.base.support.ui.font.TypefaceController
import com.maubis.scarlet.base.support.utils.DateFormatUtils
import com.maubis.scarlet.base.support.utils.Flavor
import com.maubis.scarlet.base.support.utils.ImageCache
import com.maubis.scarlet.base.support.utils.maybeThrow
import com.maubis.scarlet.base.support.utils.sDateFormat

abstract class ApplicationBase : Application() {
  override fun onCreate() {
    super.onCreate()

    // Preferences
    sAppPreferences = VersionedStore.get(this, "USER_PREFERENCES", 1)

    sBiometricManager = BiometricManager.from(this)

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
    sAppTypeface = TypefaceController(this)
  }

  companion object {
    lateinit var instance: CoreConfig

    lateinit var sAppFlavor: Flavor

    lateinit var sAppImageStorage: NoteImage
    lateinit var sAppImageCache: ImageCache

    lateinit var sAppPreferences: Store

    lateinit var sAppTheme: ThemeManager
    lateinit var sAppTypeface: TypefaceController
    lateinit var sBiometricManager: BiometricManager

    var folderSync: FolderRemoteDatabase? = null
  }
}