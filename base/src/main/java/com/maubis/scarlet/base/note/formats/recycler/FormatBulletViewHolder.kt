package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import com.github.bijoysingh.starter.util.DimensionManager
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.support.utils.getEditorActionListener

class FormatBulletViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  val bullet = findViewById<ImageView>(R.id.bullet)

  init {
    edit.setOnEditorActionListener(getEditorActionListener(
        runnable = {
          activity.createOrChangeToNextFormat(format!!)
          true
        },
        preConditions = { format === null || !edit.isFocused }
    ))
    edit.imeOptions = EditorInfo.IME_ACTION_DONE
    edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
  }

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    bullet.setColorFilter(config.secondaryTextColor)

    var sizeDp = 30
    var icon = R.drawable.bullet_1
    when (data.formatType) {
      FormatType.BULLET_2 -> {
        sizeDp = 60
        icon = R.drawable.bullet_2
      }
      FormatType.BULLET_3 -> {
        sizeDp = 90
        icon = R.drawable.bullet_3
      }
    }

    bullet.setImageResource(icon)
    (bullet.layoutParams as ViewGroup.MarginLayoutParams).marginStart = DimensionManager.dpToPixels(context, sizeDp)
  }
}