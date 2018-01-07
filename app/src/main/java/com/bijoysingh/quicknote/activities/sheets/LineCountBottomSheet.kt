package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.utils.renderMarkdown
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.LocaleManager


class LineCountBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val mainActivity = activity as MainActivity
    val dataStore = DataStore.get(context)
    var lineCount = getDefaultLineCount(dataStore)

    val lineLimit = dialog.findViewById<TextView>(R.id.line_limit)
    val reduceLineLimit = dialog.findViewById<TextView>(R.id.reduce_line_limit)
    val increaseLineLimit = dialog.findViewById<TextView>(R.id.increase_line_limit)
    val done = dialog.findViewById<TextView>(R.id.action_button)

    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)
    lineLimit.setTextColor(textColor)
    setColor(lineCount, reduceLineLimit, increaseLineLimit)
    setText(lineCount, lineLimit)
    done.setTextColor(getColor(R.color.colorAccent, R.color.colorAccentDark))

    reduceLineLimit.setOnClickListener {
      lineCount = if (lineCount <= LINE_COUNT_MIN) LINE_COUNT_MIN else (lineCount - 1)
      setColor(lineCount, reduceLineLimit, increaseLineLimit)
      setText(lineCount, lineLimit)
      dataStore.put(KEY_LINE_COUNT, lineCount)
      mainActivity.notifyAdapterExtraChanged()
    }
    increaseLineLimit.setOnClickListener {
      lineCount = if (lineCount >= LINE_COUNT_MAX) LINE_COUNT_MAX else (lineCount + 1)
      setColor(lineCount, reduceLineLimit, increaseLineLimit)
      setText(lineCount, lineLimit)
      dataStore.put(KEY_LINE_COUNT, lineCount)
      mainActivity.notifyAdapterExtraChanged()
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text))
  }

  fun setColor(count: Int, reduceLineLimit: TextView, increaseLineLimit: TextView) {
    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)
    val textDisabledColor = getColor(R.color.dark_hint_text, R.color.light_hint_text)
    reduceLineLimit.setTextColor(if (count <= LINE_COUNT_MIN) textDisabledColor else textColor)
    increaseLineLimit.setTextColor(if (count >= LINE_COUNT_MAX) textDisabledColor else textColor)
  }

  fun setText(count: Int, lineLimit: TextView) {
    lineLimit.text = LocaleManager.toString(count)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_line_count

  companion object {

    const val KEY_LINE_COUNT = "KEY_LINE_COUNT"
    const val LINE_COUNT_DEFAULT = 7
    const val LINE_COUNT_MIN = 2
    const val LINE_COUNT_MAX = 15

    fun openSheet(activity: MainActivity) {
      val sheet = LineCountBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getDefaultLineCount(dataStore: DataStore): Int = dataStore.get(KEY_LINE_COUNT, LINE_COUNT_DEFAULT)
  }
}