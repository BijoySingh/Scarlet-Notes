package com.bijoysingh.quicknote.drive

import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.maubis.scarlet.base.config.auth.IAuthenticator
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class GDriveAuthenticator() : IAuthenticator {

  var account: GoogleSignInAccount? = null

  override fun setup(context: Context) {
    GlobalScope.launch {
      val account = GoogleSignIn.getLastSignedInAccount(context)
      if (account !== null) {
        val helper = GDriveLoginActivity.getDriveHelper(context, account)
        gDrive = GDriveRemoteDatabase(WeakReference(context))
        gDrive?.init(helper)
      }
    }
  }

  override fun isLoggedIn(): Boolean {
    return account !== null
  }

  override fun userId(context: Context): String? {
    if (account !== null) {
      return account?.id
    }

    account = GoogleSignIn.getLastSignedInAccount(context)
    return account?.id
  }

  override fun openLoginActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, GDriveLoginActivity::class.java))
  }

  override fun openForgetMeActivity(context: Context) = Runnable {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  override fun logout() {

  }
}