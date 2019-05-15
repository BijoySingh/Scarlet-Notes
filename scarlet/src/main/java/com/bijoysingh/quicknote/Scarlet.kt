package com.bijoysingh.quicknote

import com.bijoysingh.quicknote.drive.GDriveRemoteDatabase
import com.bijoysingh.quicknote.firebase.FirebaseRemoteDatabase
import com.bijoysingh.quicknote.scarlet.ScarletConfig
import com.github.bijoysingh.starter.prefs.Store
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.export.support.ExternalFolderSync

class Scarlet : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()
    ApplicationBase.instance = ScarletConfig(this)
    ApplicationBase.instance.themeController().setup(this)
    ApplicationBase.instance.authenticator().setup(this)
    ApplicationBase.instance.remoteConfigFetcher().setup(this)
    ExternalFolderSync.setup(this)
  }

  companion object {
    var firebase: FirebaseRemoteDatabase? = null
    var gDrive: GDriveRemoteDatabase? = null
    var gDriveConfig: Store? = null
  }
}