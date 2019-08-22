package com.maubis.scarlet.base.core.note

import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.getFullText

enum class SortingTechnique() {
  LAST_MODIFIED,
  NEWEST_FIRST,
  OLDEST_FIRST,
  ALPHABETICAL,
  NOTE_COLOR,
  NOTE_TAGS,
}

/**
 * Helper class which allow comparison of a pair of objects
 */
class ComparablePair<T : Comparable<T>, U : Comparable<U>>(val first: T, val second: U) : Comparable<ComparablePair<T, U>> {
  override fun compareTo(other: ComparablePair<T, U>): Int {
    val firstComparison = first.compareTo(other.first)
    return when {
      firstComparison == 0 -> second.compareTo(other.second)
      else -> firstComparison
    }
  }
}

fun sort(notes: List<Note>, sortingTechnique: SortingTechnique): List<Note> {
  // Notes returned from DB are always sorted newest first. Reduce computational load
  return when (sortingTechnique) {
    SortingTechnique.LAST_MODIFIED -> notes.sortedByDescending { note ->
      if (note.pinned) Long.MAX_VALUE
      else note.updateTimestamp
    }
    SortingTechnique.OLDEST_FIRST -> notes.sortedBy { note ->
      if (note.pinned) Long.MIN_VALUE
      else note.timestamp
    }
    SortingTechnique.ALPHABETICAL -> notes.sortedBy { note ->
      val content = note.getFullText().trim().filter {
        ((it in 'a'..'z') || (it in 'A'..'Z'))
      }

      val sortValue = when {
        (note.pinned || content.isBlank()) -> 0
        else -> content[0].toUpperCase().toInt()
      }
      ComparablePair(sortValue, note.updateTimestamp)
    }
    SortingTechnique.NOTE_COLOR -> notes.sortedBy { note ->
      ComparablePair(note.color, note.updateTimestamp)
    }
    SortingTechnique.NOTE_TAGS -> {
      val tagCounterMap = HashMap<String, Int>()
      notes.map { it.getTagUUIDs() }.forEach { tags ->
        tags.forEach { tag ->
          tagCounterMap[tag] = (tagCounterMap[tag] ?: 0) + 1
        }
      }
      notes.sortedByDescending {
        val noteTagScore = it.getTagUUIDs().sumBy { tag ->
          tagCounterMap[tag] ?: 0
        }
        ComparablePair(ComparablePair(noteTagScore, it.tags), it.updateTimestamp)
      }
    }
    else -> notes.sortedByDescending { note ->
      if (note.pinned) Long.MAX_VALUE
      else note.timestamp
    }
  }
}