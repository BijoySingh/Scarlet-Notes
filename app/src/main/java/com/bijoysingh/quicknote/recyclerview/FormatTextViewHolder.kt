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
import android.widget.EditText
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.KEY_TEXT_SIZE
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.TEXT_SIZE_DEFAULT
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType.*
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
  private val actionDelete: View
  private val actionCopy: View
  private val actionPanel: View

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
    actionPanel = view.findViewById(R.id.action_panel)
    actionDelete = view.findViewById(R.id.action_delete)
    actionCopy = view.findViewById(R.id.action_copy)
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

    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    text.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT))
    text.setBackgroundColor(backgroundColor)
    text.setLinkTextColor(theme.get(context, ThemeColorType.ACCENT_TEXT))
    text.setTextIsSelectable(true)
    text.visibility = visibility(!editable)

    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    edit.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT))
    edit.setHintTextColor(theme.get(context, ThemeColorType.HINT_TEXT))
    edit.setBackgroundColor(backgroundColor)
    edit.visibility = visibility(editable)
    edit.isEnabled = editable


    root.setBackgroundColor(theme.get(context, ThemeColorType.BACKGROUND))

    actionPanel.visibility = visibility(editable)

    if (editable) {
      edit.setText(data.text)
    } else if (isMarkdownEnabled && (data.formatType == TEXT
            || data.formatType == CHECKLIST_CHECKED
            || data.formatType == CHECKLIST_UNCHECKED
            || data.formatType == QUOTE
            || data.forcedMarkdown)) {
      text.text = renderMarkdown(context, data.text)
    } else {
      text.text = data.text
    }

    actionMove.setOnClickListener {
      val areActionsVisible = actionCopy.visibility == VISIBLE
      actionCopy.visibility = visibility(!areActionsVisible)
      actionDelete.visibility = visibility(!areActionsVisible)
    }
    actionDelete.setOnClickListener { activity.deleteFormat(format!!) }
    actionCopy.setOnClickListener { TextUtils.copyToClipboard(context, edit.text.toString()) }
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

  companion object {
    val KEY_EDITABLE = "KEY_EDITABLE"
  }
}
