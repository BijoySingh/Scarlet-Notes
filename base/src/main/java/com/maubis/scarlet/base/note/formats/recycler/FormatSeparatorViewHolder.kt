package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.view.View
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.support.ui.visibility

class FormatSeparatorViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  val separator: View = root.findViewById(R.id.separator)
  val actionMove = ActionMoveIcon(root.findViewById(R.id.action_move))

  override fun populate(data: Format, config: FormatViewHolderConfig) {

    separator.setBackgroundColor(config.hintTextColor)

    actionMove.setColorFilter(config.iconColor)
    actionMove.view.visibility = visibility(config.editable)
    actionMove.view.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, config.noteUUID, data)
    }
  }
}