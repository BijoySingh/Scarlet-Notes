package com.maubis.scarlet.base.main.recycler

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ThemeColorType

class ToolbarMainRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  val title: TextView = findViewById(R.id.toolbar_title)
  val searchButton: ImageView = findViewById(R.id.toolbar_icon_search)

  override fun populate(data: RecyclerItem, extra: Bundle) {
    setFullSpan()
    searchButton.setOnClickListener {
      (context as MainActivity).setSearchMode(true)
    }

    val titleColor = CoreConfig.instance.themeController().get(ThemeColorType.PRIMARY_TEXT)
    title.setTextColor(titleColor)
    val toolbarIconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    searchButton.setColorFilter(toolbarIconColor)
  }
}

fun RecyclerViewHolder<RecyclerItem>.setFullSpan() {
  try {
    val layoutParams = itemView.getLayoutParams() as StaggeredGridLayoutManager.LayoutParams
    layoutParams.isFullSpan = true
  } catch (e: Exception) {
  }
}
