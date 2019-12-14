package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.graphics.Paint
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.support.utils.getEditorActionListener

class FormatListViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  private val icon: ImageView = root.findViewById(R.id.icon)
  private val close: ImageView = root.findViewById(R.id.close)

  init {
    edit.setOnEditorActionListener(getEditorActionListener(
      runnable = {
        activity.createOrChangeToNextFormat(format)
        true
      },
      preConditions = { !edit.isFocused }
    ))
    edit.imeOptions = EditorInfo.IME_ACTION_DONE
    edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
  }

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    icon.setColorFilter(config.iconColor)

    when (data.formatType) {
      FormatType.CHECKLIST_CHECKED -> {
        icon.setImageResource(R.drawable.ic_check_box_white_24dp)
        text.paintFlags = text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        itemView.alpha = 0.5f
      }
      FormatType.CHECKLIST_UNCHECKED -> {
        icon.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp)
        text.paintFlags = text.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        itemView.alpha = 1f
      }
      else -> {
      } // Ignore other cases
    }

    close.visibility = visibility(config.editable)
    close.setColorFilter(config.iconColor)
    close.alpha = 0.8f
    close.setOnClickListener {
      activity.deleteFormat(format)
    }

    itemView.setOnClickListener {
      if (!config.editable) {
        activity.setFormatChecked(data, data.formatType != FormatType.CHECKLIST_CHECKED)
      }
    }
    icon.setOnClickListener {
      activity.setFormatChecked(data, data.formatType != FormatType.CHECKLIST_CHECKED)
    }
  }
}
