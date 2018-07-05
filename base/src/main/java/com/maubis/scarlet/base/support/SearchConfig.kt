package com.maubis.scarlet.base.support

import com.maubis.scarlet.base.core.database.room.folder.Folder
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet
import com.maubis.scarlet.base.support.database.foldersDB
import com.maubis.scarlet.base.support.database.notesDB

data class SearchConfig(
    var text: String = "",
    var mode: HomeNavigationState = HomeNavigationState.DEFAULT,
    var colors: MutableList<Int> = emptyList<Int>().toMutableList(),
    var tags: MutableList<Tag> = emptyList<Tag>().toMutableList(),
    var folders: MutableList<Folder> = emptyList<Folder>().toMutableList())

fun isConfigFiltering(config: SearchConfig): Boolean {
  return config.folders.isNotEmpty()
      || config.tags.isNotEmpty()
      || config.colors.isNotEmpty()
      || config.mode !== HomeNavigationState.DEFAULT;
}

fun unifiedSearchSynchronous(config: SearchConfig): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = getNotesForMode(config)
      .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
      .filter { note -> config.tags.isEmpty() || config.tags.filter { note.tags !== null && note.tags.contains(it.uuid) }.isNotEmpty() }
      .filter {
        when (config.folders.isEmpty()) {
          true -> it.folder.isBlank()
          false -> config.folders.map { it.uuid }.contains(it.folder)
        }
      }
      .filter {
        when {
          config.text.isBlank() -> true
          it.locked -> false
          else -> it.getFullText().contains(config.text, true)
        }
      }
  return sort(notes, sorting)
}

fun unifiedFolderSearchSynchronous(config: SearchConfig): List<Folder> {
  if (!config.folders.isEmpty()) {
    return emptyList()
  }
  return foldersDB.getAll()
      .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
      .filter {
        when {
          config.text.isBlank() -> true
          else -> it.title.contains(config.text, true)
        }
      }
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