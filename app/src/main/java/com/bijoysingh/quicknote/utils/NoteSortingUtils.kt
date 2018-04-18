package com.bijoysingh.quicknote.utils

import com.bijoysingh.quicknote.activities.sheets.SortingTechnique
import com.maubis.scarlet.base.database.room.note.Note

fun sort(notes: List<Note>, sortingTechnique: SortingTechnique): List<Note> {
  // Notes returned from DB are always sorted newest first. Reduce computational load
  return when (sortingTechnique) {
    SortingTechnique.LAST_MODIFIED -> notes.sortedByDescending { note -> if (note.pinned) Long.MAX_VALUE else note.updateTimestamp }
    SortingTechnique.OLDEST_FIRST -> notes.sortedBy { note -> if (note.pinned) Long.MIN_VALUE else note.timestamp }
    else -> notes.sortedByDescending { note -> if (note.pinned) Long.MAX_VALUE else note.timestamp }
  }
}