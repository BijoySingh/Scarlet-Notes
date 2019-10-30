package com.bijoysingh.quicknote

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.export.support.ExternalFolderSync

class MaterialNotes : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()
    ApplicationBase.instance = MaterialNoteConfig(this)
    ExternalFolderSync.setup(this)
  }
}