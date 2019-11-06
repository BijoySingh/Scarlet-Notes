package com.maubis.scarlet.base.note.recycler

import android.content.Context
import androidx.core.content.ContextCompat
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.note.getNoteState
import com.maubis.scarlet.base.core.note.getReminderV2
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.adjustedColor
import com.maubis.scarlet.base.note.getDisplayTime
import com.maubis.scarlet.base.note.getImageFile
import com.maubis.scarlet.base.note.getLockedAwareTextForHomeList
import com.maubis.scarlet.base.note.getTagString
import com.maubis.scarlet.base.settings.sheet.sNoteItemLineCount
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ColorUtil

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  val lineCount = sNoteItemLineCount
  val backgroundColor = note.adjustedColor()
  val isLightShaded = ColorUtil.isLightColored(backgroundColor)

  val description = note.getLockedAwareTextForHomeList()
  val descriptionColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val state = note.getNoteState()
  val indicatorColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_tertiary_text)
  }

  val hasReminder = note.getReminderV2() !== null
  val actionBarIconColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_secondary_text)
    false -> ContextCompat.getColor(context, R.color.light_secondary_text)
  }

  val tagsSource = note.getTagString()
  val tags = Markdown.renderSegment(tagsSource, true)
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
  val disableBackup = note.disableBackup

  override val type = Type.NOTE
}
