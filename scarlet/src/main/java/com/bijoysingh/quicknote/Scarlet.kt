package com.bijoysingh.quicknote

import com.bijoysingh.quicknote.database.RemoteDatabaseStateController
import com.bijoysingh.quicknote.drive.GDriveRemoteDatabase
import com.bijoysingh.quicknote.firebase.FirebaseRemoteDatabase
import com.bijoysingh.quicknote.scarlet.ScarletConfig
import com.github.bijoysingh.starter.prefs.Store
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.export.support.ExternalFolderSync

class Scarlet : ApplicationBase() {

  override fun onCreate() {
    super.onCreate()

    remoteConfig = Store.get(this, "gdrive_config")

    instance = ScarletConfig(this)
    instance.authenticator().setup(this)
    instance.remoteConfigFetcher().setup(this)
    ExternalFolderSync.setup(this)
  }

  companion object {
    var firebase: FirebaseRemoteDatabase? = null
    var gDrive: GDriveRemoteDatabase? = null
    var remoteDatabaseStateController: RemoteDatabaseStateController? = null

    lateinit var remoteConfig: Store
  }
}