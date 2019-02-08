package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.support.ui.visibility

class FormatSeparatorViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view) {

  val separator: View = root.findViewById(R.id.separator)
  val actionMove: ImageView = root.findViewById(R.id.action_move_icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {

    separator.setBackgroundColor(config.hintTextColor)

    actionMove.setColorFilter(config.iconColor)
    actionMove.visibility = visibility(config.editable)
    actionMove.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, config.noteUUID, data)
    }
  }
}