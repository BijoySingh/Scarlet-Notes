package com.maubis.scarlet.base.main

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.isNoteLockedButAppUnlocked
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet

class SearchState(
  var text: String = "",
  var mode: HomeNavigationMode = HomeNavigationMode.DEFAULT,
  var currentFolder: Folder? = null,
  var colors: MutableList<Int> = emptyList<Int>().toMutableList(),
  var tags: MutableList<Tag> = emptyList<Tag>().toMutableList()) {

  fun hasFilter(): Boolean {
    return currentFolder != null
      || tags.isNotEmpty()
      || colors.isNotEmpty()
      || text.isNotBlank()
      || mode !== HomeNavigationMode.DEFAULT
  }

  fun clear(): SearchState {
    mode = HomeNavigationMode.DEFAULT
    text = ""
    colors.clear()
    tags.clear()
    currentFolder = null
    return this
  }

  fun clearSearchBar(): SearchState {
    text = ""
    colors.clear()
    tags.clear()
    return this
  }

  fun copy(): SearchState {
    return SearchState(
      text,
      mode,
      currentFolder,
      colors.filter { true }.toMutableList(),
      tags.filter { true }.toMutableList())
  }
}

fun unifiedSearchSynchronous(state: SearchState): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = unifiedSearchWithoutFolder(state)
    .filter {
      val currentFolder = state.currentFolder
      if (currentFolder == null)
        it.folder.isBlank()
      else
        currentFolder.uuid == it.folder
    }
  return sort(notes, sorting)
}

fun filterFolder(notes: List<Note>, folder: Folder): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val filteredNotes = notes.filter { it.folder == folder.uuid }
  return sort(filteredNotes, sorting)
}

fun filterOutFolders(notes: List<Note>): List<Note> {
  val allFoldersUUIDs = ApplicationBase.instance.foldersDatabase().getAll().map { it.uuid }
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val filteredNotes = notes.filter { !allFoldersUUIDs.contains(it.folder) }
  return sort(filteredNotes, sorting)
}

fun unifiedSearchWithoutFolder(state: SearchState): List<Note> {
  return getNotesForMode(state)
    .filter { state.colors.isEmpty() || state.colors.contains(it.color) }
    .filter { note -> state.tags.isEmpty() || state.tags.filter { note.tags !== null && note.tags.contains(it.uuid) }.isNotEmpty() }
    .filter {
      when {
        state.text.isBlank() -> true
        it.locked && !it.isNoteLockedButAppUnlocked() -> false
        else -> it.getFullText().contains(state.text, true)
      }
    }
}

fun filterDirectlyValidFolders(state: SearchState): List<Folder> {
  if (state.currentFolder != null) {
    return emptyList()
  }

  return foldersDb.getAll()
    .filter { state.colors.isEmpty() || state.colors.contains(it.color) }
    .filter { it.title.contains(state.text, true) }
}

fun getNotesForMode(state: SearchState): List<Note> {
  return when (state.mode) {
    HomeNavigationMode.FAVOURITE -> notesDb.getByNoteState(arrayOf(NoteState.FAVOURITE.name))
    HomeNavigationMode.ARCHIVED -> notesDb.getByNoteState(arrayOf(NoteState.ARCHIVED.name))
    HomeNavigationMode.TRASH -> notesDb.getByNoteState(arrayOf(NoteState.TRASH.name))
    HomeNavigationMode.DEFAULT -> notesDb.getByNoteState(arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name))
    HomeNavigationMode.LOCKED -> notesDb.getNoteByLocked(true)
  }
}