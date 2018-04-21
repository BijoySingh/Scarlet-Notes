package com.bijoysingh.quicknote

import android.app.Application
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.config.CoreConfig

class MaterialNotes : Application() {

  override fun onCreate() {
    super.onCreate()
    CoreConfig.instance = MaterialNoteConfig(this)
    CoreConfig.instance.themeController().setup(this)
    CoreConfig.instance.authenticator().setup(this)
    CoreConfig.instance.remoteConfigFetcher().tryFetching(this)
  }
}