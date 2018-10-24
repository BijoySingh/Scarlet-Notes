package com.bijoysingh.quicknote.firebase.support

import android.content.Context
import com.bijoysingh.quicknote.Scarlet
import com.bijoysingh.quicknote.firebase.activity.ForgetMeActivity
import com.bijoysingh.quicknote.firebase.data.FirebaseFolder
import com.github.bijoysingh.starter.util.TextUtils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.maubis.scarlet.base.core.database.room.folder.Folder
import com.maubis.scarlet.base.core.folder.FolderBuilder
import com.maubis.scarlet.base.note.folder.deleteWithoutSync
import com.maubis.scarlet.base.note.folder.saveWithoutSync
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.service.NoteBroadcast
import com.maubis.scarlet.base.service.sendNoteBroadcast
import com.maubis.scarlet.base.support.database.foldersDB
import com.maubis.scarlet.base.support.database.notesDB


/**
 * Functions for Database Reference for Firebase Notes
 */

fun folderDatabaseReference(context: Context, userId: String) {
  if (Scarlet.firebaseFolder !== null) {
    return
  }
  Scarlet.firebaseFolder = FirebaseDatabase
      .getInstance()
      .getReference()
      .child("folders")
      .child(userId)
  setListener(context)
}

fun removeFolderDatabaseReference() {
  Scarlet.firebaseFolder = null
}

fun insertFolderToFirebase(folder: FirebaseFolder) {
  if (Scarlet.firebaseFolder == null) {
    return
  }
  Scarlet.firebaseFolder!!.child(folder.uuid).setValue(folder)
}

fun deleteFolderFromFirebase(folder: FirebaseFolder) {
  if (Scarlet.firebaseFolder == null) {
    return
  }
  Scarlet.firebaseFolder!!.child(folder.uuid).removeValue()
}

private fun setListener(context: Context) {
  if (Scarlet.firebaseFolder === null) {
    return
  }

  Scarlet.firebaseFolder!!.addChildEventListener(object : ChildEventListener {
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
      handleFolderChange(snapshot, fun(folder, existingFolder, isSame) {
        if (existingFolder === null) {
          folder.saveWithoutSync()
          sendNoteBroadcast(context, NoteBroadcast.FOLDER_CHANGED, folder.uuid)
          return
        }
        if (!isSame) {
          existingFolder.title = folder.title
          existingFolder.saveWithoutSync()
          sendNoteBroadcast(context, NoteBroadcast.FOLDER_CHANGED, existingFolder.uuid)
        }
      })
    }

    override fun onChildRemoved(snapshot: DataSnapshot) {
      if (ForgetMeActivity.forgettingInProcess) {
        return
      }
      handleFolderChange(snapshot, fun(_, existingFolder, _) {
        if (existingFolder !== null) {
          existingFolder.deleteWithoutSync()
          notesDB.getAll().filter { it.folder == existingFolder.uuid }.forEach {
            it.folder = ""
            it.save(context)
          }
          sendNoteBroadcast(context, NoteBroadcast.FOLDER_DELETED, existingFolder.uuid)
        }
      })
    }

    fun handleFolderChange(
        snapshot: DataSnapshot,
        listener: (Folder, Folder?, Boolean) -> Unit) {
      try {
        val folder = snapshot.getValue(FirebaseFolder::class.java)
        if (folder === null) {
          return
        }

        val notifiedFolder = FolderBuilder().copy(folder)
        val existingFolder = foldersDB.getByUUID(folder.uuid)
        var isSame = false
        if (existingFolder !== null) {
          isSame = TextUtils.areEqualNullIsEmpty(notifiedFolder.title, existingFolder.title)
              && (notifiedFolder.color == existingFolder.color)
              && (notifiedFolder.timestamp == existingFolder.timestamp)
              && (notifiedFolder.updateTimestamp == existingFolder.updateTimestamp)
        }

        listener(notifiedFolder, existingFolder, isSame)
      } catch (e: Exception) {
        // Ignore if exception
      }
    }
  })
}