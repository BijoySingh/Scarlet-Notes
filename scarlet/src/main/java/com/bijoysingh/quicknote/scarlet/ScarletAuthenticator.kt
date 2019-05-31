package com.bijoysingh.quicknote.scarlet

import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.Scarlet
import com.bijoysingh.quicknote.drive.GDriveAuthenticator
import com.bijoysingh.quicknote.drive.GDriveLoginActivity
import com.bijoysingh.quicknote.firebase.activity.FirebaseRemovalActivity
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity
import com.bijoysingh.quicknote.firebase.support.FirebaseAuthenticator
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
    if (sGDriveLoggedIn) {
      return gdrive.userId(context)
    }
    return firebase.userId(context)
  }

  override fun setup(context: Context) {
    if (sGDriveLoggedIn) {
      gdrive.setup(context)
      return
    }
    firebase.setup(context)
  }

  override fun isLoggedIn(context: Context): Boolean {
    if (sGDriveLoggedIn) {
      return gdrive.isLoggedIn(context)
    }
    return firebase.isLoggedIn()
  }

  override fun isLegacyLoggedIn(): Boolean {
    return firebase.isLoggedIn()
  }

  override fun logout() {
    if (sGDriveLoggedIn) {
      gdrive.logout()
      return
    }
    firebase.logout()
  }

  override fun openLoginActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, GDriveLoginActivity::class.java))
  }

  override fun openForgetMeActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, ForgetMeActivity::class.java))
  }

  override fun openTransferDataActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, FirebaseRemovalActivity::class.java))
  }
}