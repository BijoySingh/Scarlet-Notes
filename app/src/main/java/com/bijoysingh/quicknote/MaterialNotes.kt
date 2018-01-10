package com.bijoysingh.quicknote

import android.app.Application
import com.bijoysingh.quicknote.database.AppDatabase
import com.bijoysingh.quicknote.utils.ThemeManager
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.google.firebase.FirebaseApp

class MaterialNotes : Application() {

  override fun onCreate() {
    super.onCreate()
    Reprint.initialize(this)

    userPreferencesVariable = VersionedStore.get(
        this,
        USER_PREFERENCES_STORE_NAME,
        USER_PREFERENCES_VERSION)
    dbVariable = AppDatabase.createDatabase(this)
    themeVariable = ThemeManager(this)

    // Temporary object, will be removed soon
    var dataStoreVariable = DataStore.get(this)
    dataStoreVariable!!.migrateToStore(userPreferencesVariable)

    FirebaseApp.initializeApp(this)
  }

  companion object {
    const val USER_PREFERENCES_STORE_NAME = "USER_PREFERENCES";
    const val USER_PREFERENCES_VERSION = 1;

    var themeVariable: ThemeManager? = null
    fun appTheme() = themeVariable!!

    var dbVariable: AppDatabase? = null
    fun db() = dbVariable!!

    var userPreferencesVariable: Store? = null
    fun userPreferences() = userPreferencesVariable!!
  }
}