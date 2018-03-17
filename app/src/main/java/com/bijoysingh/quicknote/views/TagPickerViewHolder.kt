package com.bijoysingh.quicknote.views

import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.utils.TagsDB
import com.bijoysingh.quicknote.utils.HomeNavigationState
import com.google.android.flexbox.FlexboxLayout

class TagPickerViewHolder(
    val activity: MainActivity,
    val flexbox: FlexboxLayout,
    val onClick: (Tag) -> Unit) {

  fun search(search: String) {
    setTags(TagsDB.db.search(search))
  }

  @Synchronized
  fun setTags(tags: List<Tag>) {
    flexbox.removeAllViews()
    tags.forEach {
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