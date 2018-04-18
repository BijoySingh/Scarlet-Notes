package com.bijoysingh.quicknote.database

import com.bijoysingh.quicknote.MaterialNotes.Companion.db
import com.maubis.scarlet.base.database.memory.NotesProvider
import com.maubis.scarlet.base.database.room.note.NoteDao

val notesDB = NotesDB()

class NotesDB : NotesProvider() {

  override fun database(): NoteDao = db().notes()


}