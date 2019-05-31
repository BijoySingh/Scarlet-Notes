package com.bijoysingh.quicknote.firebase.support

import com.bijoysingh.quicknote.firebase.FirebaseRemoteDatabase
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity
import com.bijoysingh.quicknote.firebase.data.FirebaseTag
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase


/**
 * Functions for Database Reference for Firebase Notes
 */

fun FirebaseRemoteDatabase.initTagReference(userId: String) {
  firebaseTag = FirebaseDatabase
      .getInstance()
      .getReference()
      .child("tags")
      .child(userId)
  setTagListener()
}

fun FirebaseRemoteDatabase.setTagListener() {
  if (firebaseTag === null) {
    return
  }
  firebaseTag!!.addChildEventListener(object : ChildEventListener {
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
        val tag = snapshot.getValue(FirebaseTag::class.java)
        if (tag === null) {
          return
        }
        onRemoteInsert(tag)
      } catch (exception: Exception) {
        // Ignore if exception
      }
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
      if (ForgetMeActivity.forgettingInProcess) {
        return
      }
      try {
        val tag = snapshot.getValue(FirebaseTag::class.java)
        if (tag === null) {
          return
        }
        // TODO: This is disabled
        // onRemoteRemove(tag)
      } catch (exception: Exception) {
        // Ignore if exception
      }
    }
  })
}