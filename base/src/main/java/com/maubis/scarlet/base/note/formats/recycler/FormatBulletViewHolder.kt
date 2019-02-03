package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.bijoysingh.starter.util.DimensionManager
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType

class FormatBulletViewHolder(context: Context, view: View) : FormatTextViewHolder(context, view) {

  val bullet = findViewById<ImageView>(R.id.bullet)

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