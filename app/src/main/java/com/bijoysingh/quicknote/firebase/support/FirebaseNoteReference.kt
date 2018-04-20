package com.bijoysingh.quicknote.firebase.support

import android.content.Context
import com.bijoysingh.quicknote.MaterialNotes
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity.Companion.forgettingInProcess
import com.bijoysingh.quicknote.database.notesDB
import com.bijoysingh.quicknote.database.utils.copyNote
import com.bijoysingh.quicknote.database.utils.deleteWithoutSync
import com.bijoysingh.quicknote.database.utils.isEqual
import com.bijoysingh.quicknote.database.utils.saveWithoutSync
import com.bijoysingh.quicknote.firebase.data.FirebaseNote
import com.bijoysingh.quicknote.utils.NoteBroadcast
import com.bijoysingh.quicknote.utils.sendNoteBroadcast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.NoteBuilder


/**
 * Functions for Database Reference for Firebase Notes
 */

fun noteDatabaseReference(context: Context, userId: String) {
  if (MaterialNotes.firebaseNote !== null) {
    return
  }
  MaterialNotes.firebaseNote = FirebaseDatabase
      .getInstance()
      .getReference()
      .child("notes")
      .child(userId)
  setListener(context)
}

fun removeNoteDatabaseReference() {
  MaterialNotes.firebaseNote = null
}

fun insertNoteToFirebase(note: FirebaseNote) {
  if (MaterialNotes.firebaseNote == null) {
    return
  }
  MaterialNotes.firebaseNote!!.child(note.uuid).setValue(note)
}

fun deleteFromFirebase(note: FirebaseNote) {
  if (MaterialNotes.firebaseNote == null) {
    return
  }
  MaterialNotes.firebaseNote!!.child(note.uuid).removeValue()
}

private fun setListener(context: Context) {
  if (MaterialNotes.firebaseNote === null) {
    return
  }
  MaterialNotes.firebaseNote!!.addChildEventListener(object : ChildEventListener {
    override fun onCancelled(p0: DatabaseError?) {
      // Ignore cancelled
    }

    override fun onChildMoved(snapshot: DataSnapshot?, p1: String?) {
      // Ignore moved child
    }

    override fun onChildChanged(snapshot: DataSnapshot?, p1: String?) {
      if (forgettingInProcess) {
        return
      }
      onChildAdded(snapshot, p1)
    }

    override fun onChildAdded(snapshot: DataSnapshot?, p1: String?) {
      if (forgettingInProcess) {
        return
      }
      handleNoteChange(snapshot, fun(note, existingNote, isSame) {
        if (existingNote === null) {
          note.saveWithoutSync(context)
          sendNoteBroadcast(context, NoteBroadcast.NOTE_CHANGED, note.uuid)
          return
        }
        if (!isSame) {
          note.uid = existingNote.uid
          existingNote.copyNote(note).saveWithoutSync(context)
          sendNoteBroadcast(context, NoteBroadcast.NOTE_CHANGED, existingNote.uuid)
        }
      })
    }

    override fun onChildRemoved(snapshot: DataSnapshot?) {
      if (forgettingInProcess) {
        return
      }
      handleNoteChange(snapshot, fun(_, existingNote, _) {
        if (existingNote !== null) {
          existingNote.deleteWithoutSync(context)
          sendNoteBroadcast(context, NoteBroadcast.NOTE_DELETED, existingNote.uuid)
        }
      })
    }

    fun handleNoteChange(
        snapshot: DataSnapshot?,
        listener: (Note, Note?, Boolean) -> Unit) {
      if (snapshot === null) {
        return
      }
      try {
        val note = snapshot.getValue(FirebaseNote::class.java)
        if (note === null) {
          return
        }

        val notifiedNote = NoteBuilder().copy(note)
        val existingNote = notesDB.existingMatch(note)
        var isSame = false
        if (existingNote !== null) {
          isSame = notifiedNote.isEqual(existingNote)
        }

        listener(notifiedNote, existingNote, isSame)
      } catch (e: Exception) {
        // Ignore if exception
      }
    }
  })
}