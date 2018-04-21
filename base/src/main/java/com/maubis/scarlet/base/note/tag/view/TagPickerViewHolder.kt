package com.maubis.scarlet.base.note.tag.view

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.main.HomeNavigationState
import com.maubis.scarlet.base.support.database.tagsDB

class TagPickerViewHolder(
    val activity: MainActivity,
    val flexbox: FlexboxLayout,
    val onClick: (Tag) -> Unit) {

  fun search(search: String) {
    setTags(tagsDB.search(search))
  }

  @Synchronized
  fun setTags(tags: List<Tag>) {
    val length = tags.size
    flexbox.removeAllViews()
    tags.subList(0, Math.min(length, 6))
        .forEach {
          val tag = it
          val tagView = View.inflate(activity, R.layout.layout_flexbox_tag_item, null) as View
          val text = tagView.findViewById<TextView>(R.id.tag_text)

          if (activity.mode == HomeNavigationState.TAG && activity.selectedTag == tag) {
            text.setBackgroundResource(R.drawable.flexbox_selected_tag_item_bg)
            text.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent))
          }

          text.text = it.title
          tagView.setOnClickListener {
            onClick(tag)
          }
          flexbox.addView(tagView)
        }
  }

  fun getTagView() {

  }
}