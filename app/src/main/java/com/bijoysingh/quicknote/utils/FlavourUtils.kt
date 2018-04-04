package com.bijoysingh.quicknote.utils

import android.util.Log
import com.bijoysingh.quicknote.BuildConfig
import com.bijoysingh.quicknote.database.external.removeNoteDatabaseReference
import com.bijoysingh.quicknote.database.external.removeTagDatabaseReference
import com.github.bijoysingh.starter.async.SimpleAsyncTask
import com.google.firebase.auth.FirebaseAuth

enum class Flavor {
  NONE, // FDroid, Master Builds
  LITE, // Play Store Version
  PRO, // Play Store Pro Version
}

fun getAppFlavor(): Flavor {
  return when (BuildConfig.FLAVOR) {
    "lite" -> Flavor.LITE
    "full" -> Flavor.PRO
    else -> Flavor.NONE
  }
}

fun firebaseUserId(): String? {
  val user = FirebaseAuth.getInstance().currentUser
  return user?.uid
}

fun isLoggedIn(): Boolean {
  return firebaseUserId() !== null
}

fun firebaseReloadUser() {
  val task = object : SimpleAsyncTask<Unit>() {
    override fun run(): Unit {
      try {
        FirebaseAuth.getInstance().currentUser?.reload()?.addOnCompleteListener {
          if (it.isSuccessful) {
            return@addOnCompleteListener
          }
          logoutUser()
        }
      } catch (e: Exception) {
        // In case somehow it fails
      }
    }

    override fun handle(t: Unit) {
      // Ignore
    }
  }
  task.execute()
}

fun logoutUser() {
  FirebaseAuth.getInstance().signOut()
  removeNoteDatabaseReference()
  removeTagDatabaseReference()
}