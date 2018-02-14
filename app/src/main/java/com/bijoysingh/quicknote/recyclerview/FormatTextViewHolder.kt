package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.View.VISIBLE
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.sheets.FormatActionBottomSheet
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.KEY_TEXT_SIZE
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.TEXT_SIZE_DEFAULT
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType.*
import com.bijoysingh.quicknote.formats.MarkdownType
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.ThemeManager
import com.bijoysingh.quicknote.utils.renderMarkdown
import com.bijoysingh.quicknote.utils.visibility
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.TextUtils

open class FormatTextViewHolder(context: Context, view: View) : RecyclerViewHolder<Format>(context, view), TextWatcher {

  protected val activity: ViewAdvancedNoteActivity
  protected val text: TextView
  protected val edit: EditText
  private val actionMove: View

  protected var format: Format? = null

  init {
    text = view.findViewById<View>(R.id.text) as TextView
    edit = view.findViewById<View>(R.id.edit) as EditText
    activity = context as ViewAdvancedNoteActivity
    edit.addTextChangedListener(this)
    edit.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> activity.focusedFormat = format }
    edit.setRawInputType(
        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            or InputType.TYPE_CLASS_TEXT
            or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
    )
    actionMove = view.findViewById(R.id.action_move)
  }

  override fun populate(data: Format, extra: Bundle?) {
    format = data

    val editable = !(extra != null
        && extra.containsKey(KEY_EDITABLE)
        && !extra.getBoolean(KEY_EDITABLE))
    val isMarkdownEnabled = (extra == null
        || extra.getBoolean(KEY_MARKDOWN_ENABLED, true)
        || data.forcedMarkdown)

    val fontSize = extra?.getInt(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
        ?: TextSizeBottomSheet.TEXT_SIZE_DEFAULT
    val theme = ThemeManager.get(context)
    val backgroundColor = when (data.formatType) {
      CODE -> theme.getThemedColor(context, R.color.material_grey_200, R.color.material_grey_700)
      else -> ContextCompat.getColor(context, R.color.transparent)
    }

    val fontSizeFloat = when(data.formatType) {
      HEADING -> fontSize.toFloat() + 4
      SUB_HEADING -> fontSize.toFloat() + 2
      else -> fontSize.toFloat()
    }
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeFloat)
    text.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT))
    text.setBackgroundColor(backgroundColor)
    text.setLinkTextColor(theme.get(context, ThemeColorType.ACCENT_TEXT))
    text.setTextIsSelectable(true)
    text.visibility = visibility(!editable)

    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeFloat)
    edit.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT))
    edit.setHintTextColor(theme.get(context, ThemeColorType.HINT_TEXT))
    edit.setBackgroundColor(backgroundColor)
    edit.visibility = visibility(editable)
    edit.isEnabled = editable

    when {
      editable -> edit.setText(data.text)
      isMarkdownEnabled -> text.text = renderMarkdown(context, data.text)
      else -> text.text = data.text
    }

    actionMove.visibility = visibility(editable)
    actionMove.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, data)
    }
  }

  override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

  }

  override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    if (format === null || !edit.isFocused) {
      return
    }
    format!!.text = s.toString()
    activity.setFormat(format!!)
  }

  override fun afterTextChanged(s: Editable) {

  }

  fun requestEditTextFocus() {
    edit.requestFocus()
  }

  fun requestMarkdownAction(markdownType: MarkdownType) {
    val cursorStartPosition = edit.selectionStart
    val cursorEndPosition = edit.selectionEnd
    val content = edit.text

    val startString = content.substring(0, cursorStartPosition)
    val middleString = content.substring(cursorStartPosition, cursorEndPosition)
    val endString = content.substring(cursorEndPosition, content.length)

    val stringBuilder = StringBuilder()
    stringBuilder.append(startString)
    stringBuilder.append(if (startString.isEmpty() || !markdownType.requiresNewLine) "" else "\n")
    stringBuilder.append(markdownType.startToken)
    stringBuilder.append(middleString)
    stringBuilder.append(markdownType.endToken)
    stringBuilder.append(endString)

    edit.setText(stringBuilder.toString())

    try {
      val additionTokenLength = (if (markdownType.requiresNewLine) 1 else 0) + markdownType.startToken.length
      edit.setSelection(Math.min(startString.length + additionTokenLength, edit.text.length))
    } catch (_: Exception) {
      // Ignore the exception
    }
  }

  companion object {
    val KEY_EDITABLE = "KEY_EDITABLE"
  }
}
