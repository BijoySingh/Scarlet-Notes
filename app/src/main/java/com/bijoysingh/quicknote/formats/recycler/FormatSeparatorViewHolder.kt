package com.bijoysingh.quicknote.formats.recycler

import android.content.Context
import android.os.Bundle
import android.view.View
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.INTENT_KEY_NOTE_ID
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.sheets.FormatActionBottomSheet
import com.maubis.scarlet.base.format.Format
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.visibility
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder

class FormatSeparatorViewHolder(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  val activity: ViewAdvancedNoteActivity = context as ViewAdvancedNoteActivity
  val separator = root.findViewById<View>(R.id.separator)
  val actionMove = root.findViewById<View>(R.id.action_move)

  init {
    separator.setBackgroundColor(appTheme().get(ThemeColorType.HINT_TEXT))
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