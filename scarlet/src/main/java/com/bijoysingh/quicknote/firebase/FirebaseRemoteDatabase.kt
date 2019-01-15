package com.bijoysingh.quicknote.firebase

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.firebase.data.FirebaseFolder
import com.bijoysingh.quicknote.firebase.data.FirebaseNote
import com.bijoysingh.quicknote.firebase.data.FirebaseTag
import com.bijoysingh.quicknote.firebase.support.initFolderReference
import com.bijoysingh.quicknote.firebase.support.initNoteReference
import com.bijoysingh.quicknote.firebase.support.initTagReference
import com.google.firebase.database.DatabaseReference
import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.tag.ITagContainer
import com.maubis.scarlet.base.database.remote.IRemoteDatabase
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import java.lang.ref.WeakReference

fun initFirebaseDatabase(context: Context, userId: String) {
  firebase = FirebaseRemoteDatabase(WeakReference(context))
  firebase?.init(userId)
}

class FirebaseRemoteDatabase(val weakContext: WeakReference<Context>) : IRemoteDatabase {

  private var isInit = false

  var firebaseNote: DatabaseReference? = null
  var firebaseTag: DatabaseReference? = null
  var firebaseFolder: DatabaseReference? = null

  @Synchronized
  override fun init(userId: String) {
    if (isInit) {
      return
    }

    initNoteReference(userId)
    initTagReference(userId)
    initFolderReference(userId)
    isInit = true
  }

  @Synchronized
  override fun reset() {
    firebaseTag = null
    firebaseNote = null
    firebaseFolder = null
    isInit = false
  }

  override fun logout() {
    // TODO: Merge with Scarlet Authenticator
    reset()
  }

  override fun deleteEverything() {
    firebaseNote?.removeValue { _, _ -> }
    firebaseTag?.removeValue { _, _ -> }
    firebaseFolder?.removeValue { _, _ -> }
  }

  override fun insert(note: INoteContainer) {
    if (note !is FirebaseNote || firebaseNote === null) {
      return
    }
    firebaseNote!!.child(note.uuid).setValue(note)
  }

  override fun insert(tag: ITagContainer) {
    if (tag !is FirebaseTag || firebaseTag === null) {
      return
    }
    firebaseTag!!.child(tag.uuid).setValue(tag)
  }

  override fun insert(folder: IFolderContainer) {
    if (folder !is FirebaseFolder || firebaseFolder === null) {
      return
    }
    firebaseFolder!!.child(folder.uuid).setValue(folder)
  }

  override fun remove(note: INoteContainer) {
    if (note !is FirebaseNote || firebaseNote === null) {
      return
    }
    firebaseNote!!.child(note.uuid).removeValue()
  }

  override fun remove(tag: ITagContainer) {
    if (tag !is FirebaseTag || firebaseTag === null) {
      return
    }
    firebaseTag!!.child(tag.uuid).removeValue()
  }

  override fun remove(folder: IFolderContainer) {
    if (folder !is FirebaseFolder || firebaseFolder === null) {
      return
    }
    firebaseFolder!!.child(folder.uuid).removeValue()
  }

  override fun onRemoteInsert(note: INoteContainer) {
    if (note !is FirebaseNote || firebaseNote === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, note)
  }

  override fun onRemoteRemove(note: INoteContainer) {
    if (note !is FirebaseNote || firebaseNote === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, note)
  }

  override fun onRemoteInsert(tag: ITagContainer) {
    if (tag !is FirebaseTag || firebaseTag === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, tag)
  }

  override fun onRemoteRemove(tag: ITagContainer) {
    if (tag !is FirebaseTag || firebaseTag === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, tag)
  }

  override fun onRemoteInsert(folder: IFolderContainer) {
    if (folder !is FirebaseFolder || firebaseFolder === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, folder)
  }

  override fun onRemoteRemove(folder: IFolderContainer) {
    if (folder !is FirebaseFolder || firebaseFolder === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, folder)
  }
}