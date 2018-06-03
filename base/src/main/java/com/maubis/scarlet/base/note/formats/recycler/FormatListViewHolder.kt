package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.graphics.Paint
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType

class FormatListViewHolder(context: Context, view: View)
  : FormatTextViewHolder(context, view), TextView.OnEditorActionListener {

  private val icon: ImageView = root.findViewById(R.id.icon)

  init {
    edit.setOnEditorActionListener(this)
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

    itemView.setOnClickListener {
      if (!config.editable) {
        activity.setFormatChecked(data, data.formatType != FormatType.CHECKLIST_CHECKED)
      }
    }
  }


  override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent?): Boolean {
    if (format === null || !edit.isFocused) {
      return false
    }

    // Ref: https://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
    if (event == null) {
      if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
        return false
      }
    } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
      if (event.action != KeyEvent.ACTION_DOWN) {
        return true
      }
    } else {
      return false
    }

    // Enter clicked
    activity.createOrChangeToNextFormat(format!!)
    return true
  }
}
