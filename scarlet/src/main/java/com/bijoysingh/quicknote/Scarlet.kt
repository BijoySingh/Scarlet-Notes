package com.bijoysingh.quicknote

import android.app.Application
import com.bijoysingh.quicknote.scarlet.ScarletConfig
import com.google.firebase.database.DatabaseReference
import com.maubis.scarlet.base.config.CoreConfig

class Scarlet : Application() {

  override fun onCreate() {
    super.onCreate()
    CoreConfig.instance = ScarletConfig(this)
    CoreConfig.instance.themeController().setup(this)
    CoreConfig.instance.authenticator().setup(this)
    CoreConfig.instance.remoteConfigFetcher().setup(this)
  }

  companion object {
    var firebaseNote: DatabaseReference? = null
    var firebaseTag: DatabaseReference? = null
  }
}