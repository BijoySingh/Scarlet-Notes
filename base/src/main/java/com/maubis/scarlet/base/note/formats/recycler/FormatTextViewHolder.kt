package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType.*
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.maubis.scarlet.base.settings.sheet.TextSizeBottomSheet
import com.maubis.scarlet.base.settings.sheet.TextSizeBottomSheet.Companion.KEY_TEXT_SIZE
import com.maubis.scarlet.base.settings.sheet.TextSizeBottomSheet.Companion.TEXT_SIZE_DEFAULT
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.utils.renderMarkdown

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
    edit.onFocusChangeListener = View.OnFocusChangeListener { _, _ -> activity.focusedFormat = format }
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
    val noteUUID: String = extra?.getString(INTENT_KEY_NOTE_ID) ?: "default"

    val fontSize = extra?.getInt(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
        ?: TextSizeBottomSheet.TEXT_SIZE_DEFAULT
    val theme = CoreConfig.instance.themeController()
    val backgroundColor = when (data.formatType) {
      CODE -> theme.get(context, R.color.material_grey_200, R.color.material_grey_700)
      else -> ContextCompat.getColor(context, R.color.transparent)
    }

    val fontSizeFloat = when (data.formatType) {
      HEADING -> fontSize.toFloat() + 4
      SUB_HEADING -> fontSize.toFloat() + 2
      else -> fontSize.toFloat()
    }
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeFloat)
    text.setTextColor(theme.get(ThemeColorType.SECONDARY_TEXT))
    text.setBackgroundColor(backgroundColor)
    text.setLinkTextColor(theme.get(ThemeColorType.ACCENT_TEXT))
    text.setTextIsSelectable(true)
    text.visibility = visibility(!editable)

    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeFloat)
    edit.setTextColor(theme.get(ThemeColorType.SECONDARY_TEXT))
    edit.setHintTextColor(theme.get(ThemeColorType.HINT_TEXT))
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
      FormatActionBottomSheet.openSheet(activity, noteUUID, data)
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
