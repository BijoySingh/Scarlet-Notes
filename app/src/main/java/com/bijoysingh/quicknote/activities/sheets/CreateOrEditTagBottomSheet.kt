package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.utils.delete
import com.bijoysingh.quicknote.database.utils.isUnsaved
import com.bijoysingh.quicknote.database.utils.save
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.getEditorActionListener


class CreateOrEditTagBottomSheet : ThemedBottomSheetFragment() {

  var selectedTag: Tag? = null
  var sheetOnTagListener: (tag: Tag, deleted: Boolean) -> Unit = { _, _ -> }

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val tag = selectedTag
    if (tag == null) {
      dismiss()
      return
    }

    val title = dialog.findViewById<TextView>(R.id.options_title)
    val action = dialog.findViewById<TextView>(R.id.action_button)
    val enterTag = dialog.findViewById<EditText>(R.id.enter_tag)
    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)

    title.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))
    action.setTextColor(theme().get(themedContext(), ThemeColorType.ACCENT_TEXT))
    enterTag.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))
    enterTag.setHintTextColor(theme().get(themedContext(), ThemeColorType.HINT_TEXT))
    removeBtn.setTextColor(theme().get(themedContext(), ThemeColorType.DISABLED_TEXT))

    title.setText(if (tag.isUnsaved()) R.string.tag_sheet_create_title else R.string.tag_sheet_edit_title)
    action.setOnClickListener {
      val updated = onActionClick(tag, enterTag.text.toString())
      sheetOnTagListener(tag, !updated)
      dismiss()
    }
    removeBtn.visibility = if (tag.isUnsaved()) GONE else VISIBLE
    removeBtn.setOnClickListener {
      tag.delete(themedContext())
      sheetOnTagListener(tag, true)
      dismiss()
    }
    enterTag.setText(tag.title)
    enterTag.setOnEditorActionListener(getEditorActionListener {
      val updated = onActionClick(tag, enterTag.text.toString())
      sheetOnTagListener(tag, !updated)
      dismiss()
      return@getEditorActionListener true
    })
  }

  private fun onActionClick(tag: Tag, title: String): Boolean {
    tag.title = title
    if (tag.title.isBlank()) {
      tag.delete(themedContext())
      return false
    }
    tag.save(themedContext())
    return true
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_or_edit_tag

  companion object {
    fun openSheet(activity: ThemedActivity, tag: Tag, listener: (tag: Tag, deleted: Boolean) -> Unit) {
      val sheet = CreateOrEditTagBottomSheet()

      sheet.selectedTag = tag
      sheet.sheetOnTagListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}