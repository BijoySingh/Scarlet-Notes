package com.maubis.scarlet.base.support.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.view.View
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig

abstract class ThemedBottomSheetFragment : SimpleBottomSheetFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val isTablet = maybeContext()?.resources?.getBoolean(R.bool.is_tablet) ?: false
    val dialog = when {
      isTablet -> BottomSheetTabletDialog(themedContext(), theme)
      else -> super.onCreateDialog(savedInstanceState)
    }
    retainInstance = true
    return dialog
  }

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }
    resetBackground(dialog)
  }

  fun themedActivity(): Activity = activity ?: context as AppCompatActivity

  fun themedContext(): Context = context ?: activity!!

  fun maybeContext(): Context? = context ?: activity

  abstract fun getBackgroundView(): Int

  fun resetBackground(dialog: Dialog) {
    val backgroundColor = CoreConfig.instance.themeController().get(ThemeColorType.BACKGROUND)
    val containerLayout = dialog.findViewById<View>(getBackgroundView())
    containerLayout.setBackgroundColor(backgroundColor)
    for (viewId in getBackgroundCardViewIds()) {
      val cardView = dialog.findViewById<CardView>(viewId)
      cardView.setCardBackgroundColor(backgroundColor)
    }
  }

  open fun getOptionsTitleColor(selected: Boolean): Int {
    val colorResource = when {
      CoreConfig.instance.themeController().isNightTheme() && selected -> R.color.material_blue_300
      CoreConfig.instance.themeController().isNightTheme() -> R.color.light_secondary_text
      selected -> R.color.material_blue_700
      else -> R.color.dark_secondary_text
    }
    return ContextCompat.getColor(themedContext(), colorResource)
  }

  fun getOptionsSubtitleColor(selected: Boolean): Int {
    val colorResource = when {
      CoreConfig.instance.themeController().isNightTheme() && selected -> R.color.material_blue_200
      CoreConfig.instance.themeController().isNightTheme() -> R.color.light_tertiary_text
      selected -> R.color.material_blue_500
      else -> R.color.dark_tertiary_text
    }
    return ContextCompat.getColor(themedContext(), colorResource)
  }

  open fun getBackgroundCardViewIds(): Array<Int> = emptyArray()

  fun makeBackgroundTransparent(dialog: Dialog, rootLayoutId: Int) {
    val containerView = dialog.findViewById<View>(getBackgroundView())
    containerView.setBackgroundColor(Color.TRANSPARENT)

    val rootView = dialog.findViewById<View>(rootLayoutId)
    val parentView = rootView.parent
    if (parentView is View) {
      parentView.setBackgroundResource(R.drawable.note_option_bs_gradient)
    }
  }
}