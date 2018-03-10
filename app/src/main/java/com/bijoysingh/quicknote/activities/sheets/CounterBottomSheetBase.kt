package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.starter.util.LocaleManager

abstract class CounterBottomSheetBase : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val mainActivity = activity as ThemedActivity
    var count = getDefaultCount()

    val lineLimit = dialog.findViewById<TextView>(R.id.line_limit)
    val reduceLineLimit = dialog.findViewById<TextView>(R.id.reduce_line_limit)
    val increaseLineLimit = dialog.findViewById<TextView>(R.id.increase_line_limit)
    val done = dialog.findViewById<TextView>(R.id.action_button)

    val textColor = theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT)
    lineLimit.setTextColor(textColor)
    setColor(count, reduceLineLimit, increaseLineLimit)
    setText(count, lineLimit)
    done.setTextColor(theme().get(themedContext(), ThemeColorType.ACCENT_TEXT))
    done.setOnClickListener {
      dismiss()
    }

    reduceLineLimit.setOnClickListener {
      count = if (count <= getMinCountLimit()) getMinCountLimit() else (count - 1)
      setColor(count, reduceLineLimit, increaseLineLimit)
      setText(count, lineLimit)
      onCountChange(dialog, mainActivity, count)
    }
    increaseLineLimit.setOnClickListener {
      count = if (count >= getMaxCountLimit()) getMaxCountLimit() else (count + 1)
      setColor(count, reduceLineLimit, increaseLineLimit)
      setText(count, lineLimit)
      setText(count, lineLimit)
      onCountChange(dialog, mainActivity, count)
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))

    setupFurther(dialog)
  }

  fun setColor(count: Int, reduceLineLimit: TextView, increaseLineLimit: TextView) {
    val textColor = theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT)
    val textDisabledColor = theme().get(themedContext(), ThemeColorType.HINT_TEXT)
    reduceLineLimit.setTextColor(if (count <= getMinCountLimit()) textDisabledColor else textColor)
    increaseLineLimit.setTextColor(if (count >= getMaxCountLimit()) textDisabledColor else textColor)
  }

  fun setText(count: Int, lineLimit: TextView) {
    lineLimit.text = LocaleManager.toString(count)
  }

  open fun setupFurther(dialog: Dialog) {}

  abstract fun getMinCountLimit(): Int

  abstract fun getMaxCountLimit(): Int

  abstract fun getDefaultCount(): Int

  abstract fun onCountChange(dialog: Dialog, activity: ThemedActivity, count: Int)

  override fun getLayout(): Int = R.layout.bottom_sheet_counter
}