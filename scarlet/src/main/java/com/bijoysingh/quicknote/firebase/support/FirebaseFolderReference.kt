package com.bijoysingh.quicknote.firebase.support

import com.bijoysingh.quicknote.firebase.FirebaseRemoteDatabase
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity
import com.bijoysingh.quicknote.firebase.data.FirebaseFolder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.maubis.scarlet.base.support.utils.maybeThrow


/**
 * Functions for Database Reference for Firebase Notes
 */
fun FirebaseRemoteDatabase.initFolderReference(userId: String) {
  firebaseFolder = FirebaseDatabase
      .getInstance()
      .getReference()
      .child("folders")
      .child(userId)
  setFolderListener()
}

fun FirebaseRemoteDatabase.setFolderListener() {
  if (firebaseFolder === null) {
    return
  }

  firebaseFolder!!.addChildEventListener(object : ChildEventListener {
    override fun onCancelled(p0: DatabaseError) {
      // Ignore cancelled
    }

    override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {
      // Ignore moved child
    }

    override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
      if (ForgetMeActivity.forgettingInProcess) {
        return
      }
      onChildAdded(snapshot, p1)
    }

    override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
      if (ForgetMeActivity.forgettingInProcess) {
        return
      }
      try {
        val folder = snapshot.getValue(FirebaseFolder::class.java)
        if (folder === null) {
          return
        }
        onRemoteInsert(folder)
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
      if (ForgetMeActivity.forgettingInProcess) {
        return
      }
      try {
        val folder = snapshot.getValue(FirebaseFolder::class.java)
        if (folder === null) {
          return
        }
        onRemoteRemove(folder)
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
    }
  })
}