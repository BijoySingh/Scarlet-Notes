package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.support.ui.visibility

class FormatBulletViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  private val firstMargin: View = root.findViewById(R.id.first_margin)
  private val secondMargin: View = root.findViewById(R.id.second_margin)
  private val icon: ImageView = root.findViewById(R.id.icon)

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    super.populate(data, config)
    icon.setColorFilter(config.iconColor)

    when (data.formatType) {
      FormatType.BULLET_1 -> {
        icon.setImageResource(R.drawable.icon_bullet_1)
        firstMargin.visibility = visibility(false)
        secondMargin.visibility = visibility(false)
      }
      FormatType.BULLET_2 -> {
        icon.setImageResource(R.drawable.icon_bullet_2)
        firstMargin.visibility = visibility(false)
        secondMargin.visibility = visibility(true)
      }
      FormatType.BULLET_3 -> {
        icon.setImageResource(R.drawable.icon_bullet_3)
        firstMargin.visibility = visibility(true)
        secondMargin.visibility = visibility(true)
      }
      else -> {
      } // Ignore other cases
    }
  }
}
