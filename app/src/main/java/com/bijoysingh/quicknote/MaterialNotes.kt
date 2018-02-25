package com.bijoysingh.quicknote

import android.app.Application
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.DataStore

class MaterialNotes : Application() {

  override fun onCreate() {
    super.onCreate()
    Reprint.initialize(this)
    dataStoreVariable = DataStore.get(this)
  }

  companion object {
    var dataStoreVariable: DataStore? = null

    fun getDataStore() = dataStoreVariable!!
  }
}