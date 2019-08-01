package com.maubis.scarlet.base.note.folder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.main.recycler.setFullSpan
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.CircleDrawable

class SelectorFolderRecyclerHolder(context: Context, view: View) : RecyclerViewHolder<RecyclerItem>(context, view) {

  protected val title: TextView
  protected val icon: ImageView

  init {
    title = view.findViewById(R.id.folder_title)
    icon = view.findViewById(R.id.folder_icon)
  }

  override fun populate(itemData: RecyclerItem, extra: Bundle?) {
    setFullSpan()

    val item = itemData as SelectorFolderRecyclerItem
    title.text = item.title
    title.setTextColor(item.titleColor)
    title.typeface = FONT_MONSERRAT
    title.alpha = 0.8f

    icon.setColorFilter(item.iconColor)
    icon.background = CircleDrawable(item.folderColor, false)
    icon.alpha = 0.8f
  }
}