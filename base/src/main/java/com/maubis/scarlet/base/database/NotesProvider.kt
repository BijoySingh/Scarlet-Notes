package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.note.NoteDao
import com.maubis.scarlet.base.note.INoteContainer
import java.util.concurrent.ConcurrentHashMap

abstract class NotesProvider {

  val notes = ConcurrentHashMap<String, Note>()

  fun notifyInsertNote(note: Note) {
    maybeLoadFromDB()
    notes[note.uuid] = note
  }

  fun notifyDelete(note: Note) {
    maybeLoadFromDB()
    notes.remove(note.uuid)
  }

  fun getCount(): Int {
    maybeLoadFromDB()
    return notes.size
  }

  fun getAll(): List<Note> {
    maybeLoadFromDB()
    return notes.values.toList()
  }

  fun getByNoteState(states: Array<String>): List<Note> {
    maybeLoadFromDB()
    return notes.values.filter { states.contains(it.state) }
  }

  fun getNoteByLocked(locked: Boolean): List<Note> {
    maybeLoadFromDB()
    return notes.values.filter { it.locked == locked }
  }

  fun getNoteByTag(uuid: String): List<Note> {
    maybeLoadFromDB()
    return notes.values.filter { it.tags?.contains(uuid) ?: false }
  }

  fun getNoteCountByTag(uuid: String): Int {
    maybeLoadFromDB()
    return notes.values.count { it.tags?.contains(uuid) ?: false }
  }

  fun getByID(uid: Int): Note? {
    maybeLoadFromDB()
    return notes.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: String): Note? {
    maybeLoadFromDB()
    return notes[uuid]
  }

  fun getAllUUIDs(): List<String> {
    maybeLoadFromDB()
    return notes.keys.toList()
  }

  fun getLastTimestamp(): Long {
    maybeLoadFromDB()
    return notes.values.map { it.updateTimestamp }.max() ?: 0
  }

  fun unlockAll() {
    maybeLoadFromDB()
  }

  fun existingMatch(noteContainer: INoteContainer): Note? {
    maybeLoadFromDB()
    return getByUUID(noteContainer.uuid())
  }

  @Synchronized
  fun maybeLoadFromDB() {
    if (notes.isNotEmpty()) {
      return
    }
    database().all.forEach {
      notes[it.uuid] = it
    }
  }

  fun clear() {
    notes.clear()
  }

  abstract fun database(): NoteDao
}