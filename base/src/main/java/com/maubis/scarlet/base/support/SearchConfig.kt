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

  fun resetMode(state: HomeNavigationState): SearchConfig {
    mode = state
    return this
  }
}

fun unifiedSearchSynchronous(config: SearchConfig): List<Note> {
  val sorting = SortingOptionsBottomSheet.getSortingState()
  val notes = filterSearchWithoutFolder(config)
      .filter {
        when (config.folders.isEmpty()) {
          true -> it.folder.isBlank()
          false -> config.folders.map { it.uuid }.contains(it.folder)
        }
      }
  return sort(notes, sorting)
}

private fun filterSearchWithoutFolder(config: SearchConfig): List<Note> {
  return getNotesForMode(config)
      .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
      .filter { note -> config.tags.isEmpty() || config.tags.filter { note.tags !== null && note.tags.contains(it.uuid) }.isNotEmpty() }
      .filter {
        when {
          config.text.isBlank() -> true
          it.locked -> false
          else -> it.getFullText().contains(config.text, true)
        }
      }
}

fun unifiedFolderSearchSynchronous(config: SearchConfig): List<Folder> {
  if (!config.folders.isEmpty()) {
    return emptyList()
  }
  if (config.text.isNotBlank() || config.tags.isNotEmpty()) {
    val folders = HashSet<Folder>()
    if (config.text.isNotBlank()) {
      folders.addAll(
          foldersDB.getAll()
              .filter { config.colors.isEmpty() || config.colors.contains(it.color) }
              .filter { it.title.contains(config.text, true) })
    }
    folders.addAll(
        filterSearchWithoutFolder(config)
            .filter { it.folder.isNotBlank() }
            .map { it.folder }
            .distinct()
            .map { foldersDB.getByUUID(it) }
            .filterNotNull())
    return folders.toList()
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