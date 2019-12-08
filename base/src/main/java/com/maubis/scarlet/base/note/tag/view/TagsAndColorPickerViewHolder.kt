package com.maubis.scarlet.base.note.tag.view

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.settings.view.ColorView

class TagsAndColorPickerViewHolder(
  val activity: MainActivity,
  val flexbox: FlexboxLayout,
  val onTagClick: (Tag) -> Unit,
  val onColorClick: (Int) -> Unit) {

  val tags = emptySet<Tag>().toMutableSet()
  val colors = emptySet<Int>().toMutableSet()

  fun reset() {
    tags.clear()
    tags.addAll(tagsDb.search(""))

    colors.clear()
    colors.addAll(notesDb.getAll().map { it.color })
  }

  fun notifyChanged() {
    setViews()
  }

  @Synchronized
  fun setViews() {
    flexbox.removeAllViews()
    setTags()
    setColors()
  }

  private fun setTags() {
    val length = tags.size
    tags.toList()
      .subList(0, Math.min(length, 6))
      .forEach {
        val tag = it
        val tagView = View.inflate(activity, R.layout.layout_flexbox_tag_item, null) as View
        val text = tagView.findViewById<TextView>(R.id.tag_text)

        if (activity.state.tags.filter { it.uuid == tag.uuid }.isNotEmpty()) {
          text.setBackgroundResource(R.drawable.flexbox_selected_tag_item_bg)
          text.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
        }

        text.text = it.title
        text.typeface = sAppTypeface.title()
        tagView.setOnClickListener {
          onTagClick(tag)
        }
        flexbox.addView(tagView)
      }
  }

  private fun setColors() {
    val length = colors.size
    colors.toList()
      .subList(0, Math.min(length, 6))
      .forEach {
        val color = it
        val colorView = ColorView(activity, R.layout.layout_color_small)
        colorView.setColor(color, activity.state.colors.contains(color))
        colorView.setOnClickListener {
          onColorClick(color)
        }
        flexbox.addView(colorView)
      }
  }
}