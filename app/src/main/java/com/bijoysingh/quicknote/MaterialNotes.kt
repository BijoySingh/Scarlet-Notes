package com.bijoysingh.quicknote

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.MaterialNoteConfig
import com.maubis.scarlet.base.export.support.ExternalFolderSync
import com.maubis.scarlet.base.support.utils.Flavor

class MaterialNotes : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()
    sAppFlavor = Flavor.NONE

    ApplicationBase.instance = MaterialNoteConfig(this)
    ExternalFolderSync.setup(this)
  }
}