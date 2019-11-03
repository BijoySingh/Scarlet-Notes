package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.maubis.markdown.Markdown
import com.maubis.markdown.spannable.clearMarkdownSpans
import com.maubis.markdown.spannable.setFormats
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.format.MarkdownType
import com.maubis.scarlet.base.note.creation.sheet.FormatActionBottomSheet
import com.maubis.scarlet.base.note.creation.sheet.sEditorLiveMarkdown
import com.maubis.scarlet.base.note.creation.sheet.sEditorMoveHandles
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.visibility
import com.maubis.scarlet.base.support.utils.maybeThrow

open class FormatTextViewHolder(context: Context, view: View) : FormatViewHolderBase(context, view), TextWatcher {

  protected val text: TextView = root.findViewById(R.id.text)
  protected val edit: EditText = root.findViewById(R.id.edit)
  protected val actionMove: ImageView = root.findViewById(R.id.action_move_icon)

  protected lateinit var format: Format

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

    val fontSize = when (data.formatType) {
      FormatType.HEADING -> config.fontSize * 1.75f
      FormatType.SUB_HEADING -> config.fontSize * 1.5f
      FormatType.HEADING_3 -> config.fontSize * 1.25f
      else -> config.fontSize
    }

    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    text.setTextColor(config.secondaryTextColor)
    text.setBackgroundColor(config.backgroundColor)
    text.setLinkTextColor(config.accentColor)
    text.setTextIsSelectable(true)
    text.setTypeface(config.typeface, config.typefaceStyle)
    text.visibility = visibility(!config.editable)

    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
    edit.setTextColor(config.secondaryTextColor)
    edit.setHintTextColor(config.hintTextColor)
    edit.setBackgroundColor(config.backgroundColor)
    edit.setTypeface(config.typeface, config.typefaceStyle)
    edit.visibility = visibility(config.editable)
    edit.isEnabled = config.editable
    showHintWhenTextIsEmpty()

    when {
      config.editable -> edit.setText(data.text)
      config.isMarkdownEnabled -> text.text = Markdown.renderSegment(data.text, true)
      else -> text.text = data.text
    }

    actionMove.setColorFilter(config.iconColor)
    actionMove.visibility = visibility(config.editable)
    actionMove.setOnClickListener {
      openSheet(activity, FormatActionBottomSheet().apply {
        noteUUID = config.noteUUID
        format = data
      })
    }
    if (config.editable && !sEditorMoveHandles) {
      actionMove.visibility = View.INVISIBLE
    }
  }

  override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {

  }

  override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
    if (!edit.isFocused) {
      return
    }

    format.text = text.toString()
    activity.setFormat(format)
    showHintWhenTextIsEmpty()
  }

  // Workaround to avoid this holder being higher than the text contained in it when hint text
  // occupies more lines than the text inserted by the user
  private fun showHintWhenTextIsEmpty() {
    edit.hint = when {
      format.text.isEmpty() -> format.getHint()
      else -> ""
    }
  }

  override fun afterTextChanged(text: Editable) {
    text.clearMarkdownSpans()
    if (sEditorLiveMarkdown) {
      text.setFormats(Markdown.getSpanInfo(format.text).spans)
    }
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
    } catch (exception: Exception) {
      maybeThrow(context as AppCompatActivity, exception)
    }
  }

  private fun Format.getHint(): String {
    return when (formatType) {
      FormatType.TEXT, FormatType.TAG -> context.getString(R.string.format_hint_text)
      FormatType.HEADING,
      FormatType.SUB_HEADING,
      FormatType.HEADING_3
      -> context.getString(R.string.format_hint_heading)
      FormatType.NUMBERED_LIST,
      FormatType.CHECKLIST_UNCHECKED,
      FormatType.CHECKLIST_CHECKED
      -> context.getString(R.string.format_hint_list)
      FormatType.CODE -> context.getString(R.string.format_hint_code)
      FormatType.QUOTE -> context.getString(R.string.format_hint_quote)
      else -> ""
    }
  }
}
