package com.maubis.scarlet.base.note.recycler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.maubis.markdown.Markdown
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.note.getNoteState
import com.maubis.scarlet.base.core.note.getReminderV2
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.note.creation.sheet.sEditorMarkdownEnabled
import com.maubis.scarlet.base.security.controller.PinLockController
import com.maubis.scarlet.base.settings.sheet.UISettingsOptionsBottomSheet.Companion.sMarkdownEnabledHome
import com.maubis.scarlet.base.settings.sheet.sNoteItemLineCount
import com.maubis.scarlet.base.settings.sheet.sSecurityAppLockEnabled
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ColorUtil

class NoteRecyclerItem(context: Context, val note: Note) : RecyclerItem() {

  private val isLightShaded = ColorUtil.isLightColored(note.color)
  val lineCount = sNoteItemLineCount

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

  override val type = RecyclerItem.Type.NOTE
}
