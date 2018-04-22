package com.bijoysingh.quicknote

import android.app.Application
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.MaterialNoteConfig

class MaterialNotes : Application() {

  override fun onCreate() {
    super.onCreate()
    CoreConfig.instance = MaterialNoteConfig(this)
    CoreConfig.instance.themeController().setup(this)
  }
}