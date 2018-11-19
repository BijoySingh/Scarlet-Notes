package com.bijoysingh.quicknote.firebase

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.firebase.data.FirebaseFolder
import com.bijoysingh.quicknote.firebase.data.FirebaseNote
import com.bijoysingh.quicknote.firebase.data.FirebaseTag
import com.bijoysingh.quicknote.firebase.support.initFolderReference
import com.bijoysingh.quicknote.firebase.support.initNoteReference
import com.bijoysingh.quicknote.firebase.support.initTagReference
import com.github.bijoysingh.starter.util.TextUtils
import com.google.firebase.database.DatabaseReference
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.core.folder.FolderBuilder
import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.isEqual
import com.maubis.scarlet.base.core.tag.ITagContainer
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.database.remote.IRemoteDatabase
import com.maubis.scarlet.base.note.deleteWithoutSync
import com.maubis.scarlet.base.note.folder.deleteWithoutSync
import com.maubis.scarlet.base.note.folder.saveWithoutSync
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.saveWithoutSync
import com.maubis.scarlet.base.note.tag.deleteWithoutSync
import com.maubis.scarlet.base.note.tag.saveWithoutSync
import com.maubis.scarlet.base.service.NoteBroadcast
import com.maubis.scarlet.base.service.sendNoteBroadcast
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

    val notifiedNote = NoteBuilder().copy(note)
    val existingNote = notesDb.existingMatch(note)
    val isSameAsExisting = existingNote !== null && notifiedNote.isEqual(existingNote)

    if (existingNote === null) {
      notifiedNote.saveWithoutSync(context)
      sendNoteBroadcast(context, NoteBroadcast.NOTE_CHANGED, note.uuid)
      return
    }
    if (!isSameAsExisting) {
      notifiedNote.uid = existingNote.uid

      val noteToSave = NoteBuilder().copy(notifiedNote)
      noteToSave.saveWithoutSync(context)
      sendNoteBroadcast(context, NoteBroadcast.NOTE_CHANGED, existingNote.uuid)
    }
  }

  override fun onRemoteRemove(note: INoteContainer) {
    if (note !is FirebaseNote || firebaseNote === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val existingNote = notesDb.existingMatch(note)
    if (existingNote !== null && !existingNote.disableBackup) {
      existingNote.deleteWithoutSync(context)
      sendNoteBroadcast(context, NoteBroadcast.NOTE_DELETED, existingNote.uuid)
    }
  }

  override fun onRemoteInsert(tag: ITagContainer) {
    if (tag !is FirebaseTag || firebaseTag === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val notifiedTag = TagBuilder().copy(tag)
    val existingTag = tagsDb.getByUUID(tag.uuid)
    var isSameAsExisting = existingTag !== null
        && TextUtils.areEqualNullIsEmpty(notifiedTag.title, existingTag.title)

    if (existingTag === null) {
      notifiedTag.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.TAG_CHANGED, tag.uuid)
      return
    }
    if (!isSameAsExisting) {
      existingTag.title = tag.title
      existingTag.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.TAG_CHANGED, existingTag.uuid)
    }
  }

  override fun onRemoteRemove(tag: ITagContainer) {
    if (tag !is FirebaseTag || firebaseTag === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val existingTag = tagsDb.getByUUID(tag.uuid)
    if (existingTag !== null) {
      existingTag.deleteWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.TAG_DELETED, existingTag.uuid)
    }
  }

  override fun onRemoteInsert(folder: IFolderContainer) {
    if (folder !is FirebaseFolder || firebaseFolder === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val notifiedFolder = FolderBuilder().copy(folder)
    val existingFolder = foldersDb.getByUUID(folder.uuid)
    var isSameAsExisting = existingFolder !== null
        && TextUtils.areEqualNullIsEmpty(notifiedFolder.title, existingFolder.title)
        && (notifiedFolder.color == existingFolder.color)
        && (notifiedFolder.timestamp == existingFolder.timestamp)
        && (notifiedFolder.updateTimestamp == existingFolder.updateTimestamp)

    if (existingFolder === null) {
      notifiedFolder.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.FOLDER_CHANGED, folder.uuid)
      return
    }
    if (!isSameAsExisting) {
      existingFolder.title = folder.title
      existingFolder.saveWithoutSync()
      sendNoteBroadcast(context, NoteBroadcast.FOLDER_CHANGED, existingFolder.uuid)
    }
  }

  override fun onRemoteRemove(folder: IFolderContainer) {
    if (folder !is FirebaseFolder || firebaseFolder === null) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val existingFolder = foldersDb.getByUUID(folder.uuid)
    if (existingFolder !== null) {
      existingFolder.deleteWithoutSync()
      notesDb.getAll().filter { it.folder == existingFolder.uuid }.forEach {
        it.folder = ""
        it.save(context)
      }
      sendNoteBroadcast(context, NoteBroadcast.FOLDER_DELETED, existingFolder.uuid)
    }
  }
}