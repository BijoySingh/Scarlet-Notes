package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateOrEditAdvancedNoteActivity
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.visibility
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder

class FormatSeparatorViewHolder(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  val separator = root.findViewById<View>(R.id.separator)
  val actionMove = root.findViewById<View>(R.id.action_move)
  val actionDelete = root.findViewById<View>(R.id.action_remove)

  init {
    separator.setBackgroundColor(appTheme().get(ThemeColorType.HINT_TEXT))
  }

  override fun populate(data: Format, extra: Bundle?) {
    val editable = !(extra != null
        && extra.containsKey(FormatTextViewHolder.KEY_EDITABLE)
        && !extra.getBoolean(FormatTextViewHolder.KEY_EDITABLE))
    actionMove.visibility = visibility(editable)

    actionDelete.visibility = visibility(editable)
    actionDelete.setOnClickListener {
      val activity = context as CreateOrEditAdvancedNoteActivity
      activity.deleteFormat(data)
    }
  }
}