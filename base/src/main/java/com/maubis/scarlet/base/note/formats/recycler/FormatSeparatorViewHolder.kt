package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.visibility

class FormatSeparatorViewHolder(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  val activity: ViewAdvancedNoteActivity = context as ViewAdvancedNoteActivity
  val separator = root.findViewById<View>(R.id.separator)
  val actionMove = root.findViewById<View>(R.id.action_move)

  init {
    separator.setBackgroundColor(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))
  }

  override fun populate(data: Format, extra: Bundle?) {
    val noteUUID: String = extra?.getString(INTENT_KEY_NOTE_ID) ?: "default"
    val editable = !(extra != null
        && extra.containsKey(FormatTextViewHolder.KEY_EDITABLE)
        && !extra.getBoolean(FormatTextViewHolder.KEY_EDITABLE))
    actionMove.visibility = visibility(editable)
    actionMove.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, noteUUID, data)
    }
  }
}