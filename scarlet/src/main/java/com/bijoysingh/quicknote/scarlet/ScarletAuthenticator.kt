package com.bijoysingh.quicknote.scarlet

import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.Scarlet
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.database.RemoteDatabaseStateController
import com.bijoysingh.quicknote.drive.*
import com.bijoysingh.quicknote.firebase.activity.FirebaseRemovalActivity
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity
import com.bijoysingh.quicknote.firebase.support.FirebaseAuthenticator
import com.maubis.scarlet.base.config.auth.IAuthenticator
import com.maubis.scarlet.base.config.auth.IPendingUploadListener
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.ThemedActivity

const val KEY_G_DRIVE_LOGGED_IN = "g_drive_logged_in"
var sGDriveLoggedIn: Boolean
  get() = Scarlet.remoteConfig.get(KEY_G_DRIVE_LOGGED_IN, false)
  set(value) = Scarlet.remoteConfig.put(KEY_G_DRIVE_LOGGED_IN, value)

const val KEY_FIREBASE_KILLED = "firebase_killed_v2"
var sFirebaseKilled: Boolean
  get() = Scarlet.remoteConfig.get(KEY_FIREBASE_KILLED, false)
  set(value) = Scarlet.remoteConfig.put(KEY_FIREBASE_KILLED, value)

class ScarletAuthenticator() : IAuthenticator {
  val firebase = FirebaseAuthenticator()
  val gdrive = GDriveAuthenticator()

  override fun userId(context: Context): String? {
    if (shouldIgnoreFirebase()) {
      return gdrive.userId(context)
    }
    return firebase.userId(context)
  }

  override fun setup(context: Context) {
    Scarlet.remoteDatabaseStateController = RemoteDatabaseStateController(context)
    if (shouldIgnoreFirebase()) {
      gdrive.setup(context)
      return
    }
    firebase.setup(context)
  }

  override fun isLoggedIn(context: Context): Boolean {
    if (shouldIgnoreFirebase()) {
      return gdrive.isLoggedIn(context)
    }
    return firebase.isLoggedIn()
  }

  override fun isLegacyLoggedIn(): Boolean {
    return !shouldIgnoreFirebase() && firebase.isLoggedIn()
  }

  override fun logout() {
    if (shouldIgnoreFirebase()) {
      gdrive.logout()
      return
    }
    firebase.logout()
  }

  override fun setPendingUploadListener(listener: IPendingUploadListener?) {
    if (shouldIgnoreFirebase()) {
      gDrive?.setPendingUploadListener(listener)
    }
  }

  override fun requestSync(forced: Boolean) {
    if (shouldIgnoreFirebase()) {
      gDrive?.resync(forced)
    }
  }

  private fun shouldIgnoreFirebase() = sFirebaseKilled || sGDriveLoggedIn

  override fun openLoginActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, GDriveLoginActivity::class.java))
  }

  override fun openForgetMeActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, ForgetMeActivity::class.java))
  }

  override fun openTransferDataActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, FirebaseRemovalActivity::class.java))
  }

  override fun openLogoutActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, GDriveLogoutActivity::class.java))
  }

  override fun showPendingSync(activity: ThemedActivity) {
    if (shouldIgnoreFirebase()) {
      openSheet(activity, GDrivePendingBottomSheet())
    }
  }
}