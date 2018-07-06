package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.support.v4.content.ContextCompat
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.database.room.folder.Folder
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.note.getNoteState
import com.maubis.scarlet.base.core.note.getReminder
import com.maubis.scarlet.base.note.*
import com.maubis.scarlet.base.settings.sheet.LineCountBottomSheet
import com.maubis.scarlet.base.settings.sheet.MarkdownBottomSheet
import com.maubis.scarlet.base.support.database.foldersDB
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ColorUtil
import ru.noties.markwon.Markwon

class FolderRecyclerItem(context: Context,
                         val folder: Folder,
                         val click: () -> Unit = {},
                         val longClick: () -> Unit = {},
                         val selected: Boolean = false) : RecyclerItem() {

  val isLightShaded = ColorUtil.isLightColored(folder.color)
  val title = folder.title
  val titleColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_tertiary_text)
    false -> ContextCompat.getColor(context, R.color.light_primary_text)
  }

  val timestamp = folder.getDisplayTime()
  val timestampColor = when (isLightShaded) {
    true -> ContextCompat.getColor(context, R.color.dark_hint_text)
    false -> ContextCompat.getColor(context, R.color.light_hint_text)
  }

  val usage = notesDB.getNoteCountByFolder(folder.uuid)

  override val type = RecyclerItem.Type.FOLDER
}
