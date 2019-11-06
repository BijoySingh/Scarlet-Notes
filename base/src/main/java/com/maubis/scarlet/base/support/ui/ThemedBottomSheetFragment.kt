package com.maubis.scarlet.base.support.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme

abstract class ThemedBottomSheetFragment : SimpleBottomSheetFragment() {

  var appContext: Context? = null

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
    appContext = dialog.context.applicationContext
    resetBackground(dialog)
  }

  fun themedActivity(): Activity = activity ?: context as AppCompatActivity

  fun themedContext(): Context = maybeContext()!!

  fun maybeContext(): Context? = context ?: activity ?: appContext

  abstract fun getBackgroundView(): Int

  fun resetBackground(dialog: Dialog) {
    val backgroundColor = sAppTheme.get(ThemeColorType.BACKGROUND)
    val containerLayout = dialog.findViewById<View>(getBackgroundView())
    containerLayout.setBackgroundColor(backgroundColor)
    for (viewId in getBackgroundCardViewIds()) {
      val cardView = dialog.findViewById<CardView>(viewId)
      cardView.setCardBackgroundColor(backgroundColor)
    }
  }

  open fun getOptionsTitleColor(selected: Boolean): Int {
    val colorResource = when {
      sAppTheme.isNightTheme() && selected -> R.color.material_blue_300
      sAppTheme.isNightTheme() -> R.color.light_secondary_text
      selected -> R.color.material_blue_700
      else -> R.color.dark_secondary_text
    }
    return ContextCompat.getColor(themedContext(), colorResource)
  }

  open fun getOptionsSubtitleColor(selected: Boolean): Int {
    val colorResource = when {
      sAppTheme.isNightTheme() && selected -> R.color.material_blue_200
      sAppTheme.isNightTheme() -> R.color.light_tertiary_text
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

  companion object {
    fun openSheet(activity: MainActivity, sheet: ThemedBottomSheetFragment) {
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}