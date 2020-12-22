package com.maubis.scarlet.base.note.activity

import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.database.room.note.Note

interface INoteOptionSheetActivity {
  fun updateNote(note: Note)

  fun markItem(note: Note, state: NoteState)

  fun moveItemToTrashOrDelete(note: Note)

  fun notifyTagsChanged(note: Note)

  fun getSelectMode(note: Note): String

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}