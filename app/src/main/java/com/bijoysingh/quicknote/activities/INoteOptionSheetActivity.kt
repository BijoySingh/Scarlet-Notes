package com.bijoysingh.quicknote.activities

import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.utils.NoteState

interface INoteOptionSheetActivity {
  fun updateNote(note: Note)

  fun markItem(note: Note, state: NoteState)

  fun moveItemToTrashOrDelete(note: Note)

  fun notifyTagsChanged(note: Note)

  fun getSelectMode(note: Note): String

  fun notifyResetOrDismiss()

  fun lockedContentIsHidden(): Boolean
}