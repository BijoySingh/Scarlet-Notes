package com.maubis.scarlet.base.support.database

import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.NotesProvider
import com.maubis.scarlet.base.database.room.note.NoteDao

val notesDB: NotesProvider get() = CoreConfig.instance.notesDatabase()

class NotesDB : NotesProvider() {

  override fun database(): NoteDao = CoreConfig.instance.database().notes()


}