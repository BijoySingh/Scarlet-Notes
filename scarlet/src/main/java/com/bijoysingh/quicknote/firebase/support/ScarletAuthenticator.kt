package com.bijoysingh.quicknote.firebase.support

import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity
import com.bijoysingh.quicknote.firebase.activity.LoginActivity
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.maubis.scarlet.base.auth.IAuthenticator

class ScarletAuthenticator() : IAuthenticator {
  override fun userId(): String? {
    val user = FirebaseAuth.getInstance().currentUser
    return user?.uid
  }

  override fun setup(context: Context) {
    FirebaseApp.initializeApp(context)
    try {
      val userId = userId()
      if (userId === null) {
        return
      }

      FirebaseDatabase.getInstance()
      noteDatabaseReference(context, userId)
      tagDatabaseReference(context, userId)
      reloadUser()
    } catch (exception: Exception) {
      // Don't need to do anything
    }
  }

  override fun isLoggedIn(): Boolean {
    return userId() !== null
  }

  override fun logout() {
    FirebaseAuth.getInstance().signOut()
    removeNoteDatabaseReference()
    removeTagDatabaseReference()
  }

  override fun openLoginActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, LoginActivity::class.java))
  }

  override fun openForgetMeActivity(context: Context) = Runnable {
    context.startActivity(Intent(context, ForgetMeActivity::class.java))
  }

  fun reloadUser() {
    val task = SimpleThreadExecutor.execute {
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
        }
      } catch (e: Exception) {
        // In case somehow it fails
      }
    }
  }
}