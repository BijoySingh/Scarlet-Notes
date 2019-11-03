package com.maubis.scarlet.base.main.recycler

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.MainActivityActions
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.performAction
import com.maubis.scarlet.base.settings.sheet.InternalSettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.support.utils.maybeThrow

class ToolbarMainRecyclerHolder(context: Context, itemView: View) : RecyclerViewHolder<RecyclerItem>(context, itemView) {

  val toolbarTitle: TextView = findViewById(R.id.toolbarTitle)
  val toolbarIconSearch: ImageView = findViewById(R.id.toolbarIconSearch)
  val toolbarIconSettings: ImageView = findViewById(R.id.toolbarIconSettings)
  val toolbarIconDebug: ImageView = findViewById(R.id.toolbarIconDebug)

  override fun populate(data: RecyclerItem, extra: Bundle) {
    setFullSpan()
    toolbarIconSearch.setOnClickListener {
      (context as MainActivity).setSearchMode(true)
    }

    toolbarIconSettings.setOnClickListener {
      SettingsOptionsBottomSheet.openSheet((context as MainActivity))
    }

    val titleColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    toolbarTitle.setTextColor(titleColor)
    toolbarTitle.typeface = sAppTypeface.heading()

    val toolbarIconColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    toolbarIconSearch.setColorFilter(toolbarIconColor)
    toolbarIconSettings.setColorFilter(toolbarIconColor)

    toolbarIconDebug.visibility = visibility(BuildConfig.DEBUG)
    toolbarIconDebug.setColorFilter(toolbarIconColor)
    toolbarIconDebug.setOnClickListener {
      (context as MainActivity).performAction(MainActivityActions.TYPEFACE_PICKER)
    }
  }
}

fun RecyclerViewHolder<RecyclerItem>.setFullSpan() {
  try {
    val layoutParams = itemView.getLayoutParams() as StaggeredGridLayoutManager.LayoutParams
    layoutParams.isFullSpan = true
  } catch (exception: Exception) {
    maybeThrow(itemView.context as AppCompatActivity, exception)
  }
}
