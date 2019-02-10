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
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ThemeColorType

class ToolbarMainRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  val toolbarTitle: TextView = findViewById(R.id.toolbarTitle)
  val toolbarIconSearch: ImageView = findViewById(R.id.toolbarIconSearch)
  val toolbarIconSettings: ImageView = findViewById(R.id.toolbarIconSettings)

  override fun populate(data: RecyclerItem, extra: Bundle) {
    setFullSpan()
    toolbarIconSearch.setOnClickListener {
      (context as MainActivity).setSearchMode(true)
    }

    toolbarIconSettings.setOnClickListener {
      SettingsOptionsBottomSheet.openSheet((context as MainActivity))
    }

    val titleColor = CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT)
    toolbarTitle.setTextColor(titleColor)

    val toolbarIconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    toolbarIconSearch.setColorFilter(toolbarIconColor)
  }
}

fun RecyclerViewHolder<RecyclerItem>.setFullSpan() {
  try {
    val layoutParams = itemView.getLayoutParams() as StaggeredGridLayoutManager.LayoutParams
    layoutParams.isFullSpan = true
  } catch (e: Exception) {
  }
}
