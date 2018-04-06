package com.bijoysingh.quicknote.items

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet
import com.bijoysingh.quicknote.activities.sheets.MarkdownBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.*
import ru.noties.markwon.Markwon

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  val isLightShaded = ColorUtils.calculateLuminance(note.color) > 0.35
  val isMarkdownEnabled = MarkdownBottomSheet.isMarkdownEnabled()
      && MarkdownBottomSheet.isMarkdownHomeEnabled()
  val lineCount = LineCountBottomSheet.getDefaultLineCount()

  val title = note.getMarkdownTitle(context, isMarkdownEnabled)
  val titleColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val description = note.getLockedText(context, isMarkdownEnabled)
  val descriptionColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val state = note.getNoteState()
  val indicatorColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_tertiary_text)
  }

  val hasReminder = note.getReminder() !== null
  val actionBarIconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val tagsSource = note.getTagString()
  val tags = Markwon.markdown(context, tagsSource)
  val tagsColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val timestamp = note.getDisplayTime()
  val timestampColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_hint_text)
    false -> ContextCompat.getColor(context, R.color.light_hint_text)
  }

  val imageSource = note.getImageFile()

  override val type = RecyclerItem.Type.NOTE
}
