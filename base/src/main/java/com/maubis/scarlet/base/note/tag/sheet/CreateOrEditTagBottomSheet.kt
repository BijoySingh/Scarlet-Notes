package com.maubis.scarlet.base.note.tag.sheet

import android.app.Dialog
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.core.tag.isUnsaved
import com.maubis.scarlet.base.note.tag.delete
import com.maubis.scarlet.base.note.tag.save
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment
import com.maubis.scarlet.base.utils.getEditorActionListener


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

    title.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    enterTag.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    enterTag.setHintTextColor(CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT))

    title.setText(if (tag.isUnsaved()) R.string.tag_sheet_create_title else R.string.tag_sheet_edit_title)
    action.setOnClickListener {
      val updated = onActionClick(tag, enterTag.text.toString())
      sheetOnTagListener(tag, !updated)
      dismiss()
    }
    removeBtn.visibility = if (tag.isUnsaved()) GONE else VISIBLE
    removeBtn.setOnClickListener {
      tag.delete()
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
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  private fun onActionClick(tag: Tag, title: String): Boolean {
    tag.title = title
    if (tag.title.isBlank()) {
      tag.delete()
      return false
    }
    tag.save()
    return true
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_create_or_edit_tag

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.content_card)

  companion object {
    fun openSheet(activity: ThemedActivity, tag: Tag, listener: (tag: Tag, deleted: Boolean) -> Unit) {
      val sheet = CreateOrEditTagBottomSheet()

      sheet.selectedTag = tag
      sheet.sheetOnTagListener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}