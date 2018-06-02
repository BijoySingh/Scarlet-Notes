package com.maubis.scarlet.base.support

import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.support.database.notesDB

data class SearchConfig(
    var text: String = "",
    var mode: HomeNavigationState = HomeNavigationState.DEFAULT,
    var colors: MutableList<Int> = emptyList<Int>().toMutableList(),
    var tags: MutableList<Tag> = emptyList<Tag>().toMutableList())

fun unifiedSearchSynchronous(config: SearchConfig): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = getNotesForMode(config)
      .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
      .filter { note -> config.tags.isEmpty() || config.tags.filter { note.tags !== null && note.tags.contains(it.uuid) }.isNotEmpty() }
      .filter {
        when {
          config.text.isBlank() -> true
          it.locked -> false
          else -> it.getFullText().contains(config.text, true)
        }
      }
  return sort(notes, sorting)
}

fun getNotesForMode(config: SearchConfig): List<Note> {
  return when (config.mode) {
    HomeNavigationState.FAVOURITE -> notesDB.getByNoteState(arrayOf(NoteState.FAVOURITE.name))
    HomeNavigationState.ARCHIVED -> notesDB.getByNoteState(arrayOf(NoteState.ARCHIVED.name))
    HomeNavigationState.TRASH -> notesDB.getByNoteState(arrayOf(NoteState.TRASH.name))
    HomeNavigationState.DEFAULT -> notesDB.getByNoteState(arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name))
    else -> throw Exception("Invalid Search Mode")
  }
}