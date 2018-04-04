package com.bijoysingh.quicknote

import android.app.Application
import com.bijoysingh.quicknote.database.AppDatabase
import com.bijoysingh.quicknote.utils.ThemeManager
import com.bijoysingh.quicknote.database.external.noteDatabaseReference
import com.bijoysingh.quicknote.database.external.tagDatabaseReference
import com.bijoysingh.quicknote.utils.firebaseReloadUser
import com.bijoysingh.quicknote.utils.firebaseUserId
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.prefs.Store
import com.github.bijoysingh.starter.prefs.VersionedStore
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
    setupFirebase()
  }

  private fun setupFirebase() {
    try {
      val userId = firebaseUserId()
      if (userId === null) {
        return
      }

      FirebaseDatabase.getInstance()
      noteDatabaseReference(this, userId)
      tagDatabaseReference(this, userId)

      firebaseReloadUser()
    } catch (exception: Exception) {
      // Don't need to do anything
    }
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

    var firebaseNote: DatabaseReference? = null
    var firebaseTag: DatabaseReference? = null
  }
}