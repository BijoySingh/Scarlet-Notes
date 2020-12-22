package com.bijoysingh.quicknote.firebase.support

import com.bijoysingh.quicknote.firebase.FirebaseRemoteDatabase
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity.Companion.forgettingInProcess
import com.bijoysingh.quicknote.firebase.data.FirebaseNote
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.maubis.scarlet.base.support.utils.maybeThrow

/**
 * Functions for Database Reference for Firebase Notes
 */
fun FirebaseRemoteDatabase.initNoteReference(userId: String) {
  firebaseNote = FirebaseDatabase
    .getInstance()
    .getReference()
    .child("notes")
    .child(userId)
  setNoteListener()
}

fun FirebaseRemoteDatabase.setNoteListener() {
  if (firebaseNote === null) {
    return
  }
  firebaseNote!!.addChildEventListener(object : ChildEventListener {
    override fun onCancelled(p0: DatabaseError) {
      // Ignore cancelled
    }

    override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {
      // Ignore moved child
    }

    override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
      if (forgettingInProcess) {
        return
      }
      onChildAdded(snapshot, p1)
    }

    override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
      if (forgettingInProcess) {
        return
      }

      try {
        val note = snapshot.getValue(FirebaseNote::class.java)
        if (note === null) {
          return
        }
        onRemoteInsert(note)
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
      if (forgettingInProcess) {
        return
      }

      try {
        val note = snapshot.getValue(FirebaseNote::class.java)
        if (note === null) {
          return
        }
        onRemoteRemove(note)
      } catch (exception: Exception) {
        maybeThrow(exception)
      }
    }
  })
}