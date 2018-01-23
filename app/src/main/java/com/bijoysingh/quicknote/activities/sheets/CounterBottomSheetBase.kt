package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.github.bijoysingh.starter.prefs.DataStore
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
    val dataStore = DataStore.get(context)
    var count = getDefaultCount(dataStore)

    val lineLimit = dialog.findViewById<TextView>(R.id.line_limit)
    val reduceLineLimit = dialog.findViewById<TextView>(R.id.reduce_line_limit)
    val increaseLineLimit = dialog.findViewById<TextView>(R.id.increase_line_limit)
    val done = dialog.findViewById<TextView>(R.id.action_button)

    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)
    lineLimit.setTextColor(textColor)
    setColor(count, reduceLineLimit, increaseLineLimit)
    setText(count, lineLimit)
    done.setTextColor(getColor(R.color.colorAccent, R.color.colorAccentDark))
    done.setOnClickListener {
      dismiss()
    }

    reduceLineLimit.setOnClickListener {
      count = if (count <= getMinCountLimit()) getMinCountLimit() else (count - 1)
      setColor(count, reduceLineLimit, increaseLineLimit)
      setText(count, lineLimit)
      onCountChange(dialog, mainActivity, dataStore, count)
    }
    increaseLineLimit.setOnClickListener {
      count = if (count >= getMaxCountLimit()) getMaxCountLimit() else (count + 1)
      setColor(count, reduceLineLimit, increaseLineLimit)
      setText(count, lineLimit)
      setText(count, lineLimit)
      onCountChange(dialog, mainActivity, dataStore, count)
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text))

    setupFurther(dialog, dataStore)
  }

  fun setColor(count: Int, reduceLineLimit: TextView, increaseLineLimit: TextView) {
    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)
    val textDisabledColor = getColor(R.color.dark_hint_text, R.color.light_hint_text)
    reduceLineLimit.setTextColor(if (count <= getMinCountLimit()) textDisabledColor else textColor)
    increaseLineLimit.setTextColor(if (count >= getMaxCountLimit()) textDisabledColor else textColor)
  }

  fun setText(count: Int, lineLimit: TextView) {
    lineLimit.text = LocaleManager.toString(count)
  }

  open fun setupFurther(dialog: Dialog, dataStore: DataStore) {}

  abstract fun getMinCountLimit(): Int

  abstract fun getMaxCountLimit(): Int

  abstract fun getDefaultCount(dataStore: DataStore): Int

  abstract fun onCountChange(dialog: Dialog, activity: ThemedActivity, dataStore: DataStore, count: Int)

  override fun getLayout(): Int = R.layout.bottom_sheet_counter
}