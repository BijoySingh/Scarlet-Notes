package com.maubis.scarlet.base.note.activity

import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.note.NoteState

interface INoteOptionSheetActivity {
  fun updateNote(note: Note)

  fun markItem(note: Note, state: NoteState)

  fun moveItemToTrashOrDelete(note: Note)

  fun notifyTagsChanged(note: Note)

  fun getSelectMode(note: Note): String

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}