package com.bijoysingh.quicknote.activities

import com.bijoysingh.quicknote.database.Note

interface INoteSelectorActivity {
  fun onNoteClicked(note: Note)

  fun isNoteSelected(note: Note): Boolean
}