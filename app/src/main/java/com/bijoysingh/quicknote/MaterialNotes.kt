package com.bijoysingh.quicknote

import android.app.Application
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore

class MaterialNotes : Application() {

  override fun onCreate() {
    super.onCreate()
    Reprint.initialize(this)
    userPreferencesVariable = VersionedStore.get(this, USER_PREFERENCES_STORE_NAME, USER_PREFERENCES_VERSION)

    // Temporary object, will be removed soon
    var dataStoreVariable = DataStore.get(this)
    dataStoreVariable!!.migrateToStore(userPreferencesVariable)
  }

  companion object {
    const val USER_PREFERENCES_STORE_NAME = "USER_PREFERENCES";
    const val USER_PREFERENCES_VERSION = 1;

    var userPreferencesVariable: Store? = null

    fun userPreferences() = userPreferencesVariable!!
  }
}