package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class GDriveAuthenticator {

  var hasAccountSetup: AtomicBoolean = AtomicBoolean(false)
  var account: GoogleSignInAccount? = null

  fun setup(context: Context) {
    GlobalScope.launch {
      account = GoogleSignIn.getLastSignedInAccount(context)
      hasAccountSetup.set(true)

      val signInAccount = account
      if (signInAccount !== null) {
        val helper = GDriveLoginActivity.getDriveHelper(context, signInAccount)
        gDrive = GDriveRemoteDatabase(WeakReference(context))
        gDrive?.init(helper)
      }
    }
  }

  fun isLoggedIn(context: Context): Boolean {
    if (hasAccountSetup.get()) {
      return account !== null
    }

    account = GoogleSignIn.getLastSignedInAccount(context)
    hasAccountSetup.set(true)

    return account !== null
  }

  fun userId(context: Context): String? {
    if (!hasAccountSetup.get()) {
      account = GoogleSignIn.getLastSignedInAccount(context)
      hasAccountSetup.set(true)
    }

    return account?.id
  }

  fun logout() {

  }
}