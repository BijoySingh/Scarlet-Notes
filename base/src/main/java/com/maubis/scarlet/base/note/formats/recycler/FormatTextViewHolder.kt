package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.support.utils.renderMarkdown

open class FormatTextViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view), TextWatcher {

  protected val text: TextView = root.findViewById(R.id.text)
  protected val edit: EditText = root.findViewById(R.id.edit)
  protected val actionMove = ActionMoveIcon(root.findViewById(R.id.action_move))

  protected var format: Format? = null

  init {
    edit.addTextChangedListener(this)
    edit.onFocusChangeListener = View.OnFocusChangeListener { _, _ -> activity.focusedFormat = format }
    edit.setRawInputType(
        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            or InputType.TYPE_CLASS_TEXT
            or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
    )
  }

  override fun populate(data: Format, config: FormatViewHolderConfig) {
    format = data

    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.fontSize)
    text.setTextColor(config.secondaryTextColor)
    text.setBackgroundColor(config.backgroundColor)
    text.setLinkTextColor(config.accentColor)
    text.setTextIsSelectable(true)
    text.visibility = visibility(!config.editable)

    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.fontSize)
    edit.setTextColor(config.secondaryTextColor)
    edit.setHintTextColor(config.hintTextColor)
    edit.setBackgroundColor(config.backgroundColor)
    edit.visibility = visibility(config.editable)
    edit.isEnabled = config.editable

    when {
      config.editable -> edit.setText(data.text)
      config.isMarkdownEnabled -> text.text = renderMarkdown(context, data.text)
      else -> text.text = data.text
    }

    actionMove.setColorFilter(config.iconColor)
    actionMove.view.visibility = visibility(config.editable)
    actionMove.view.setOnClickListener {
      FormatActionBottomSheet.openSheet(activity, config.noteUUID, data)
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
}
