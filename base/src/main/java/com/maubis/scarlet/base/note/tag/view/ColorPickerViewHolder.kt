package com.maubis.scarlet.base.note.tag.view

import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.settings.view.ColorView

class ColorPickerViewHolder(
    val activity: MainActivity,
    val flexbox: FlexboxLayout,
    val onClick: (Int) -> Unit) {
  @Synchronized
  fun setColors(colors: List<Int>) {
    val length = colors.size
    flexbox.removeAllViews()
    colors.subList(0, Math.min(length, 6))
        .forEach {
          val color = it
          val colorView = ColorView(activity)
          colorView.setColor(color, false)
          colorView.setOnClickListener {
            onClick(color)
          }
          flexbox.addView(colorView)
        }
  }
}