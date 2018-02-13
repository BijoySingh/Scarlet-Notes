package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.Companion.KEY_MARKDOWN_ENABLED
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.KEY_TEXT_SIZE
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.TEXT_SIZE_DEFAULT
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.formats.FormatType.CODE
import com.bijoysingh.quicknote.formats.MarkdownType
import com.bijoysingh.quicknote.recyclerview.FormatTextViewHolder.Companion.KEY_EDITABLE
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.ThemeManager
import com.bijoysingh.quicknote.utils.renderMarkdown
import com.bijoysingh.quicknote.utils.visibility
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.TextUtils

class FormatImageViewHolder(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  protected val activity: ViewAdvancedNoteActivity
  protected val text: TextView
  protected val image: ImageView
  private val actionMove: View

  protected var format: Format? = null

  init {
    text = view.findViewById<View>(R.id.text) as TextView
    image = view.findViewById<View>(R.id.image) as ImageView
    activity = context as ViewAdvancedNoteActivity
    actionMove = view.findViewById(R.id.action_move)
  }

  override fun populate(data: Format, extra: Bundle?) {
    format = data
    val editable = !(extra != null
        && extra.containsKey(KEY_EDITABLE)
        && !extra.getBoolean(KEY_EDITABLE))
    val fontSize = extra?.getInt(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
        ?: TextSizeBottomSheet.TEXT_SIZE_DEFAULT

    val theme = ThemeManager.get(context)
    val backgroundColor =
        theme.getThemedColor(context, R.color.material_grey_200, R.color.material_grey_700)

    text.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT))
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
    text.setBackgroundColor(backgroundColor)
    text.visibility = visibility(!editable)

    text.setOnClickListener {

    }

    actionMove.setOnClickListener {

    }
  }
}
