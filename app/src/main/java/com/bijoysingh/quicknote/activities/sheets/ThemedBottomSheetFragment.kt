package com.bijoysingh.quicknote.activities.sheets

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.ThemeManager
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment

abstract class ThemedBottomSheetFragment : SimpleBottomSheetFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    setRetainInstance(true)
    return dialog
  }

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }
    setBackgroundView(dialog, getBackgroundView())
  }

  fun themedActivity(): Activity = activity ?: context as AppCompatActivity

  fun themedContext(): Context = context ?: activity!!

  abstract fun getBackgroundView(): Int

  private fun setBackgroundView(dialog: Dialog, viewId: Int) {
    if (isNightMode()) {
      val containerLayout = dialog.findViewById<View>(viewId);
      containerLayout.setBackgroundColor(theme().get(themedContext(), ThemeColorType.BACKGROUND))
    }
  }

  fun theme() = ThemeManager.get(themedContext())

  fun getColor(lightColorRes: Int, darkColorRes: Int): Int =
      theme().getThemedColor(themedContext(), lightColorRes, darkColorRes)

  fun maybeSetTextNightModeColor(dialog: Dialog, viewId: Int, colorId: Int) {
    if (isNightMode()) {
      val textView = dialog.findViewById<TextView>(viewId);
      textView.setTextColor(ContextCompat.getColor(themedContext(), colorId))
    }
  }

  // Remove once done
  fun isNightMode() = theme().isNightTheme()

  fun getOptionsTitleColor(selected: Boolean): Int {
    val colorResource = when {
      isNightMode() && selected -> R.color.material_blue_300
      isNightMode() -> R.color.light_secondary_text
      selected -> R.color.material_blue_700
      else -> R.color.dark_secondary_text
    }
    return ContextCompat.getColor(themedContext(), colorResource)
  }

  fun getOptionsSubtitleColor(selected: Boolean): Int {
    val colorResource = when {
      isNightMode() && selected -> R.color.material_blue_200
      isNightMode() -> R.color.light_tertiary_text
      selected -> R.color.material_blue_500
      else -> R.color.dark_tertiary_text
    }
    return ContextCompat.getColor(themedContext(), colorResource)
  }
}