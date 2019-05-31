package com.bijoysingh.quicknote.firebase.support

import android.content.Context
import com.bijoysingh.quicknote.Scarlet
import com.bijoysingh.quicknote.drive.GDriveAuthenticator
import com.maubis.scarlet.base.config.auth.IAuthenticator

const val KEY_G_DRIVE_LOGGED_IN = "g_drive_logged_in"
var sGDriveLoggedIn: Boolean
  get() = Scarlet.gDriveConfig?.get(KEY_G_DRIVE_LOGGED_IN, false) ?: false
  set(value) = Scarlet.gDriveConfig?.put(KEY_G_DRIVE_LOGGED_IN, value) ?: Unit

const val KEY_FIREBASE_KILLED = "firebase_killed"
var sFirebaseKilled: Boolean
  get() = Scarlet.gDriveConfig?.get(KEY_FIREBASE_KILLED, false) ?: false
  set(value) = Scarlet.gDriveConfig?.put(KEY_FIREBASE_KILLED, value) ?: Unit

class ScarletAuthenticator() : IAuthenticator {

  val firebase = FirebaseAuthenticator()
  val gdrive = GDriveAuthenticator()

  override fun userId(context: Context): String? {
    return firebase.userId(context)
  }

  override fun setup(context: Context) {
    firebase.setup(context)
  }

  override fun isLoggedIn(): Boolean {
    return firebase.isLoggedIn()
  }

  override fun logout() {
    firebase.logout()
  }

  override fun openLoginActivity(context: Context) = gdrive.openLoginActivity(context)

  override fun openForgetMeActivity(context: Context) = firebase.openForgetMeActivity(context)
}