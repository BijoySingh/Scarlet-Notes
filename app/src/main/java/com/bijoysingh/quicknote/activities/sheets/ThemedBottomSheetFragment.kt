package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment

abstract class ThemedBottomSheetFragment : SimpleBottomSheetFragment() {

  var isNightMode: Boolean = false

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }
    setBackgroundView(dialog, getBackgroundView())
  }

  abstract fun getBackgroundView(): Int

  private fun setBackgroundView(dialog: Dialog, viewId: Int) {
    if (isNightMode) {
      val containerLayout = dialog.findViewById<View>(viewId);
      containerLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.material_grey_800))
    }
  }

  fun getColor(lightColorRes: Int, darkColorRes: Int): Int {
    return ContextCompat.getColor(
        context,
        when (isNightMode) {
          true -> darkColorRes
          else -> lightColorRes
        })
  }

  fun maybeSetTextNightModeColor(dialog: Dialog, viewId: Int, colorId: Int) {
    if (isNightMode) {
      val textView = dialog.findViewById<TextView>(viewId);
      textView.setTextColor(ContextCompat.getColor(context, colorId))
    }
  }

  fun getOptionsTitleColor(selected: Boolean): Int {
    val colorResource = when {
      isNightMode && selected -> R.color.material_blue_300
      isNightMode -> R.color.light_secondary_text
      selected -> R.color.material_blue_700
      else -> R.color.dark_secondary_text
    }
    return ContextCompat.getColor(context, colorResource)
  }

  fun getOptionsSubtitleColor(selected: Boolean): Int {
    val colorResource = when {
      isNightMode && selected -> R.color.material_blue_200
      isNightMode -> R.color.light_tertiary_text
      selected -> R.color.material_blue_500
      else -> R.color.dark_tertiary_text
    }
    return ContextCompat.getColor(context, colorResource)
  }
}