package com.maubis.scarlet.base.support

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.note.sort
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.note.getFullText
import com.maubis.scarlet.base.note.isNoteLockedButAppUnlocked
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet

class SearchConfig(
  var text: String = "",
  var mode: HomeNavigationState = HomeNavigationState.DEFAULT,
  var colors: MutableList<Int> = emptyList<Int>().toMutableList(),
  var tags: MutableList<Tag> = emptyList<Tag>().toMutableList(),
  var folders: MutableList<Folder> = emptyList<Folder>().toMutableList()) {

  fun hasFolder(folder: Folder) = folders.firstOrNull { it.uuid == folder.uuid } !== null

  fun hasFilter(): Boolean {
    return folders.isNotEmpty()
      || tags.isNotEmpty()
      || colors.isNotEmpty()
      || text.isNotBlank()
      || mode !== HomeNavigationState.DEFAULT;
  }

  fun clear(): SearchConfig {
    mode = HomeNavigationState.DEFAULT
    text = ""
    colors.clear()
    tags.clear()
    folders.clear()
    return this
  }

  fun clearSearchBar(): SearchConfig {
    text = ""
    colors.clear()
    tags.clear()
    return this
  }

  fun resetMode(state: HomeNavigationState): SearchConfig {
    mode = state
    return this
  }

  fun copy(): SearchConfig {
    return SearchConfig(
      text,
      mode,
      colors.filter { true }.toMutableList(),
      tags.filter { true }.toMutableList(),
      folders.filter { true }.toMutableList())
  }
}

fun unifiedSearchSynchronous(config: SearchConfig): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = unifiedSearchWithoutFolder(config)
    .filter {
      when (config.folders.isEmpty()) {
        true -> it.folder.isBlank()
        false -> config.folders.map { it.uuid }.contains(it.folder)
      }
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

fun unifiedSearchWithoutFolder(config: SearchConfig): List<Note> {
  return getNotesForMode(config)
    .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
    .filter { note -> config.tags.isEmpty() || config.tags.filter { note.tags !== null && note.tags.contains(it.uuid) }.isNotEmpty() }
    .filter {
      when {
        config.text.isBlank() -> true
        it.locked && !it.isNoteLockedButAppUnlocked() -> false
        else -> it.getFullText().contains(config.text, true)
      }
    }
}

fun filterDirectlyValidFolders(config: SearchConfig): List<Folder> {
  if (!config.folders.isEmpty()) {
    return emptyList()
  }

  return foldersDb.getAll()
    .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
    .filter { it.title.contains(config.text, true) }
}

fun getNotesForMode(config: SearchConfig): List<Note> {
  return when (config.mode) {
    HomeNavigationState.FAVOURITE -> notesDb.getByNoteState(arrayOf(NoteState.FAVOURITE.name))
    HomeNavigationState.ARCHIVED -> notesDb.getByNoteState(arrayOf(NoteState.ARCHIVED.name))
    HomeNavigationState.TRASH -> notesDb.getByNoteState(arrayOf(NoteState.TRASH.name))
    HomeNavigationState.DEFAULT -> notesDb.getByNoteState(arrayOf(NoteState.DEFAULT.name, NoteState.FAVOURITE.name))
    HomeNavigationState.LOCKED -> notesDb.getNoteByLocked(true)
  }
}