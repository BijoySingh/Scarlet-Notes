package com.bijoysingh.quicknote.firebase.support

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.firebase.initFirebaseDatabase
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.github.bijoysingh.starter.util.ToastHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.main.recycler.KEY_FORCE_SHOW_SIGN_IN
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class FirebaseAuthenticator() {

  var hasUidSetup: AtomicBoolean = AtomicBoolean(false)
  var userId: String? = null

  fun userId(context: Context): String? {
    val user = FirebaseAuth.getInstance().currentUser
    return user?.uid
  }

  fun setup(context: Context) {
    GlobalScope.launch {
      FirebaseApp.initializeApp(context)
      try {
        userId = userId(context)
        hasUidSetup.set(true)
        if (userId === null) {
          return@launch
        }

        FirebaseDatabase.getInstance()
        initFirebaseDatabase(context, userId!!)
        reloadUser(context)
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
    }
  }

  fun isLoggedIn(): Boolean {
    if (hasUidSetup.get()) {
      return userId !== null
    }

    userId = FirebaseAuth.getInstance().currentUser?.uid
    hasUidSetup.set(true)
    return userId !== null
  }

  fun logout() {
    userId = null
    FirebaseAuth.getInstance().signOut()
    firebase?.logout()
  }

  private fun reloadUser(context: Context) {
    SimpleThreadExecutor.execute {
      try {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
          if (it.isSuccessful) {
            return@addOnCompleteListener
          }
          val exception = it.exception
          if (exception !== null && exception is FirebaseNetworkException) {
            return@addOnCompleteListener
          }

          logout()
          ApplicationBase.instance.store().put(KEY_FORCE_SHOW_SIGN_IN, true)
          val handler = Handler(Looper.getMainLooper())
          handler.post {
            ToastHelper.show(context, "You have been signed out of the app")
          }
        }
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
    }
  }
}